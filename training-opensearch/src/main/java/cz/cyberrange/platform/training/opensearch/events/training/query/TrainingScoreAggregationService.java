package cz.cyberrange.platform.training.opensearch.events.training.query;

import cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions.OpenSearchQueryException;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.opensearch.events.training.model.index.OpensearchTrainingEventIndexBuilder;
import cz.cyberrange.platform.training.opensearch.events.training.query.projection.RunEventAggregate;
import cz.cyberrange.platform.training.opensearch.events.training.query.projection.RunScoreSnapshot;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.LongTermsBucket;
import org.opensearch.client.opensearch._types.aggregations.MultiBucketBase;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Aggregates the training events of one instance into one projection per training run.
 *
 * <p>This service is infrastructure-only. The caller names the runs, the levels, the event type
 * whose latest occurrence states a level's score, and the event types to count; nothing about which
 * levels award score, how long an instance lasts, or how runs rank is decided here.
 *
 * <p>Both level and run dimensions are filtered by the sets they are sized from, so a returned
 * bucket list shorter than the requested set means those runs or levels produced no such events,
 * never that an aggregation truncated.
 */
@Service
public class TrainingScoreAggregationService {

  private static final String TIMESTAMP_FIELD = "timestamp";
  private static final String TRAINING_RUN_ID_FIELD = "training_run_id";
  private static final String TRAINING_INSTANCE_ID_FIELD = "training_instance_id";
  private static final String TYPE_KEYWORD_FIELD = "type.keyword";
  private static final String LEVEL_FIELD = "level";

  private static final String BY_RUN_AGGREGATION = "by_run";
  private static final String BY_LEVEL_AGGREGATION = "by_level";
  private static final String BY_TYPE_AGGREGATION = "by_type";
  private static final String LATEST_AGGREGATION = "latest";
  private static final String COMPLETIONS_AGGREGATION = "completions";
  private static final String COUNTED_AGGREGATION = "counted";

  private static final String QUERY_FAILED_MSG = "OpenSearch score aggregation failed.";

  private final OpenSearchClient openSearchClient;

  @Autowired
  public TrainingScoreAggregationService(OpenSearchClient openSearchClient) {
    this.openSearchClient = openSearchClient;
  }

  /**
   * Aggregates every requested run of an instance in one search: the standing at each run's most
   * recent event, the score at each run's most recent completion of each level, and the occurrences
   * of each counted event type broken down by level.
   *
   * @param instanceId training instance id; selects the instance-scoped index.
   * @param runIds the runs to cover; an empty collection yields an empty result without querying.
   * @param levelIds the levels to cover, bounding both the completion and the count breakdowns.
   * @param completionEventType the event type whose latest occurrence on a level states the score
   *     reached on that level.
   * @param countedEventTypes the event types to count per run and level.
   * @return projection keyed by run id, omitting runs that produced no events at all.
   */
  public Map<Long, RunEventAggregate> aggregateRuns(
      Long instanceId,
      Collection<Long> runIds,
      Collection<Long> levelIds,
      String completionEventType,
      Collection<String> countedEventTypes) {
    if (runIds.isEmpty()) {
      return Map.of();
    }
    Aggregation byRun =
        Aggregation.of(
            aggregation ->
                aggregation
                    .terms(terms -> terms.field(TRAINING_RUN_ID_FIELD).size(runIds.size()))
                    .aggregations(LATEST_AGGREGATION, latestEvent())
                    .aggregations(
                        COMPLETIONS_AGGREGATION, completionsBranch(levelIds, completionEventType))
                    .aggregations(COUNTED_AGGREGATION, countedBranch(levelIds, countedEventTypes)));

    Aggregate runs = search(instanceId, scopedQuery(instanceId, runIds), byRun);
    if (runs == null) {
      return Map.of();
    }

    Map<Long, RunEventAggregate> aggregates = new HashMap<>();
    for (LongTermsBucket runBucket : runs.lterms().buckets().array()) {
      aggregates.put(Long.parseLong(runBucket.key()), toAggregate(runBucket));
    }
    return aggregates;
  }

  /**
   * Reads one run's bucket into the projection, leaving the standing null when it holds no event
   */
  private static RunEventAggregate toAggregate(LongTermsBucket runBucket) {
    RunScoreSnapshot latestScore =
        latestEventOf(runBucket)
            .map(
                event ->
                    new RunScoreSnapshot(
                        event.getTotalTrainingScore(), event.getTotalAssessmentScore()))
            .orElse(null);
    return new RunEventAggregate(
        latestScore, scoreByLevel(runBucket), countByEventTypeAndLevel(runBucket));
  }

  /**
   * Takes the score each level ended on from the newest completion event within that level, and
   * yields nothing for a level that was never completed
   */
  private static Map<Long, Integer> scoreByLevel(LongTermsBucket runBucket) {
    Map<Long, Integer> scoreByLevelId = new HashMap<>();
    Aggregate completions = runBucket.aggregations().get(COMPLETIONS_AGGREGATION);
    if (completions == null) {
      return scoreByLevelId;
    }
    for (LongTermsBucket levelBucket :
        completions.filter().aggregations().get(BY_LEVEL_AGGREGATION).lterms().buckets().array()) {
      latestEventOf(levelBucket)
          .ifPresent(
              event ->
                  scoreByLevelId.put(
                      Long.parseLong(levelBucket.key()), event.getActualScoreInLevel()));
    }
    return scoreByLevelId;
  }

  /**
   * Reads the per-level document counts of each counted event type, omitting a type or a level that
   * produced nothing rather than recording a zero
   */
  private static Map<String, Map<Long, Long>> countByEventTypeAndLevel(LongTermsBucket runBucket) {
    Map<String, Map<Long, Long>> countByEventType = new HashMap<>();
    Aggregate counted = runBucket.aggregations().get(COUNTED_AGGREGATION);
    if (counted == null) {
      return countByEventType;
    }
    for (StringTermsBucket typeBucket :
        counted.filter().aggregations().get(BY_TYPE_AGGREGATION).sterms().buckets().array()) {
      Map<Long, Long> countByLevelId = new HashMap<>();
      for (LongTermsBucket levelBucket :
          typeBucket.aggregations().get(BY_LEVEL_AGGREGATION).lterms().buckets().array()) {
        countByLevelId.put(Long.parseLong(levelBucket.key()), levelBucket.docCount());
      }
      countByEventType.put(typeBucket.key(), countByLevelId);
    }
    return countByEventType;
  }

  /**
   * Recovers the single newest event a bucket kept, empty when the bucket kept none or its document
   * carried no body
   */
  private static Optional<AbstractAuditPOJO> latestEventOf(MultiBucketBase bucket) {
    Aggregate latest = bucket.aggregations().get(LATEST_AGGREGATION);
    if (latest == null) {
      return Optional.empty();
    }
    List<Hit<JsonData>> hits = latest.topHits().hits().hits();
    if (hits.isEmpty() || hits.get(0).source() == null) {
      return Optional.empty();
    }
    return Optional.of(hits.get(0).source().to(AbstractAuditPOJO.class));
  }

  private static Aggregation completionsBranch(
      Collection<Long> levelIds, String completionEventType) {
    return Aggregation.of(
        aggregation ->
            aggregation
                .filter(
                    matchesAll(
                        matchesAnyString(TYPE_KEYWORD_FIELD, List.of(completionEventType)),
                        matchesAnyLong(LEVEL_FIELD, levelIds)))
                .aggregations(
                    BY_LEVEL_AGGREGATION,
                    groupByLevel(levelIds.size(), LATEST_AGGREGATION, latestEvent())));
  }

  private static Aggregation countedBranch(
      Collection<Long> levelIds, Collection<String> countedEventTypes) {
    return Aggregation.of(
        aggregation ->
            aggregation
                .filter(
                    matchesAll(
                        matchesAnyString(TYPE_KEYWORD_FIELD, countedEventTypes),
                        matchesAnyLong(LEVEL_FIELD, levelIds)))
                .aggregations(
                    BY_TYPE_AGGREGATION,
                    Aggregation.of(
                        byType ->
                            byType
                                .terms(
                                    terms ->
                                        terms
                                            .field(TYPE_KEYWORD_FIELD)
                                            .size(countedEventTypes.size()))
                                .aggregations(
                                    BY_LEVEL_AGGREGATION,
                                    groupByLevel(levelIds.size(), null, null)))));
  }

  private static Aggregation groupByLevel(int levelCount, String nestedName, Aggregation nested) {
    int size = Math.max(1, levelCount);
    if (nestedName == null) {
      return Aggregation.of(
          aggregation -> aggregation.terms(terms -> terms.field(LEVEL_FIELD).size(size)));
    }
    return Aggregation.of(
        aggregation ->
            aggregation
                .terms(terms -> terms.field(LEVEL_FIELD).size(size))
                .aggregations(nestedName, nested));
  }

  private static Aggregation latestEvent() {
    return Aggregation.of(
        aggregation ->
            aggregation.topHits(
                topHits ->
                    topHits
                        .size(1)
                        .sort(
                            sort ->
                                sort.field(
                                    field -> field.field(TIMESTAMP_FIELD).order(SortOrder.Desc)))));
  }

  private static Query scopedQuery(Long instanceId, Collection<Long> runIds) {
    Query matchesInstance =
        Query.of(
            query ->
                query.term(
                    term ->
                        term.field(TRAINING_INSTANCE_ID_FIELD).value(FieldValue.of(instanceId))));
    return matchesAll(matchesInstance, matchesAnyLong(TRAINING_RUN_ID_FIELD, runIds));
  }

  private static Query matchesAll(Query... clauses) {
    return Query.of(query -> query.bool(bool -> bool.filter(List.of(clauses))));
  }

  private static Query matchesAnyLong(String field, Collection<Long> values) {
    return Query.of(
        query ->
            query.terms(
                terms ->
                    terms
                        .field(field)
                        .terms(
                            candidates ->
                                candidates.value(values.stream().map(FieldValue::of).toList()))));
  }

  private static Query matchesAnyString(String field, Collection<String> values) {
    return Query.of(
        query ->
            query.terms(
                terms ->
                    terms
                        .field(field)
                        .terms(
                            candidates ->
                                candidates.value(values.stream().map(FieldValue::of).toList()))));
  }

  private Aggregate search(Long instanceId, Query query, Aggregation groupedByRun) {
    String instanceIndex =
        OpensearchTrainingEventIndexBuilder.builder().instance(instanceId).build();
    SearchRequest searchRequest =
        SearchRequest.of(
            request ->
                request
                    .index(instanceIndex)
                    .ignoreUnavailable(true)
                    .allowNoIndices(true)
                    .size(0)
                    .query(query)
                    .aggregations(BY_RUN_AGGREGATION, groupedByRun));
    try {
      SearchResponse<Void> response = openSearchClient.search(searchRequest, Void.class);
      return response.aggregations() == null
          ? null
          : response.aggregations().get(BY_RUN_AGGREGATION);
    } catch (IOException | OpenSearchException e) {
      throw new OpenSearchQueryException(QUERY_FAILED_MSG, e);
    }
  }
}
