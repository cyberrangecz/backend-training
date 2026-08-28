package cz.cyberrange.platform.training.opensearch.events.training.query;

import cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions.OpenSearchQueryException;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.opensearch.events.training.model.index.OpensearchTrainingEventIndexBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Queries and deletes training audit events stored in OpenSearch, scoped by training run or
 * training instance
 */
@Service
public class TrainingEventsService {
  private static final String TIMESTAMP_FIELD = "timestamp";
  private static final String TRAINING_RUN_ID_FIELD = "training_run_id";
  private static final String TRAINING_INSTANCE_ID_FIELD = "training_instance_id";
  private static final String TYPE_FIELD = "type";
  private static final String LEVEL_FIELD = "level";
  private static final String USER_REF_ID_FIELD = "user_ref_id";
  private static final String QUERY_FAILED_MSG = "OpenSearch query failed.";
  private static final String DELETE_FAILED_MSG = "OpenSearch delete failed.";

  private final OpenSearchClient openSearchClient;

  @Autowired
  public TrainingEventsService(OpenSearchClient openSearchClient) {
    this.openSearchClient = openSearchClient;
  }

  /**
   * Retrieves every audit event recorded for a training run, matched by run id alone across every
   * pool, sandbox, definition, and instance segment of the index name.
   *
   * @param trainingRunId id of the training run whose events are fetched
   * @return events ordered by ascending timestamp, oldest first; an empty list if none match
   * @throws OpenSearchQueryException if the OpenSearch query fails
   */
  public List<AbstractAuditPOJO> findAllEventsFromTrainingRun(Long trainingRunId) {
    String index = OpensearchTrainingEventIndexBuilder.builder().run(trainingRunId).build();
    return searchAllEvents(index);
  }

  /**
   * Deletes the OpenSearch index holding events for one training run within one training instance.
   *
   * @param trainingInstanceId id of the training instance the run belongs to
   * @param trainingRunId id of the training run whose event index is deleted
   */
  public void deleteEventsFromTrainingRun(Long trainingInstanceId, Long trainingRunId) {
    String index =
        OpensearchTrainingEventIndexBuilder.builder()
            .instance(trainingInstanceId)
            .run(trainingRunId)
            .build();
    deleteIndex(index);
  }

  /**
   * Deletes the OpenSearch index holding events for every run of one training instance.
   *
   * @param trainingInstanceId id of the training instance whose event index is deleted
   */
  public void deleteEventsByTrainingInstanceId(Long trainingInstanceId) {
    String index =
        OpensearchTrainingEventIndexBuilder.builder().instance(trainingInstanceId).build();
    deleteIndex(index);
  }

  /**
   * Checks whether any event has been recorded for a training run.
   *
   * @param trainingRunId id of the training run to check
   * @return true if at least one event exists, false otherwise
   * @throws OpenSearchQueryException if the OpenSearch query fails
   */
  public boolean hasRunEvents(Long trainingRunId) {
    String index = OpensearchTrainingEventIndexBuilder.builder().run(trainingRunId).build();
    try {
      SearchResponse<Void> response =
          openSearchClient.search(
              s -> s.index(index).ignoreUnavailable(true).allowNoIndices(true).size(0), Void.class);
      return response.hits().total() != null && response.hits().total().value() > 0;
    } catch (IOException | OpenSearchException e) {
      throw new OpenSearchQueryException(QUERY_FAILED_MSG, e);
    }
  }

  /**
   * Fetches training events for a specific instance from the instance-scoped OpenSearch index,
   * filtered by event type and timestamp. When {@code userRefIdFilter} is non-null, an additional
   * {@code user_ref_id} term filter is applied at query level — callers use this to restrict which
   * user's events are returned without post-fetch filtering.
   *
   * <p>This method is infrastructure-only: it has no knowledge of which event types require
   * user-level restriction. That decision belongs to the caller.
   *
   * @param instanceId training instance id; selects the instance-scoped index
   * @param eventType OpenSearch type discriminator string (e.g. {@code "level_started"})
   * @param sinceTimestampMs epoch milliseconds lower bound (exclusive); only events with {@code
   *     timestamp > sinceTimestampMs} are returned
   * @param userRefIdFilter when non-null, restricts results to events where {@code user_ref_id ==
   *     userRefIdFilter}; when null, no user filter
   * @return list of matching events, never null
   */
  public List<AbstractAuditPOJO> findFilteredTrainingEvents(
      Long instanceId, String eventType, long sinceTimestampMs, Long userRefIdFilter) {
    String instanceIndex =
        OpensearchTrainingEventIndexBuilder.builder().instance(instanceId).build();

    Query mustMatchInstance =
        Query.of(
            clause ->
                clause.term(
                    term ->
                        term.field(TRAINING_INSTANCE_ID_FIELD).value(FieldValue.of(instanceId))));
    Query mustMatchEventType =
        Query.of(
            clause -> clause.term(term -> term.field(TYPE_FIELD).value(FieldValue.of(eventType))));
    Query mustBeAfterTimestamp =
        Query.of(
            clause ->
                clause.range(
                    range -> range.field(TIMESTAMP_FIELD).gt(JsonData.of(sinceTimestampMs))));

    BoolQuery.Builder boolQueryBuilder =
        new BoolQuery.Builder()
            .must(mustMatchInstance)
            .must(mustMatchEventType)
            .must(mustBeAfterTimestamp);

    if (userRefIdFilter != null) {
      Query mustMatchUser =
          Query.of(
              clause ->
                  clause.term(
                      term -> term.field(USER_REF_ID_FIELD).value(FieldValue.of(userRefIdFilter))));
      boolQueryBuilder.must(mustMatchUser);
    }

    Query filteredQuery = boolQueryBuilder.build()._toQuery();

    SearchRequest searchRequest =
        SearchRequest.of(
            request ->
                request
                    .index(instanceIndex)
                    .ignoreUnavailable(true)
                    .allowNoIndices(true)
                    .sort(
                        sort ->
                            sort.field(field -> field.field(TIMESTAMP_FIELD).order(SortOrder.Asc)))
                    .query(filteredQuery));

    return executeSearch(searchRequest);
  }

  /**
   * Searches every document in the given index or index pattern with no query filter, in ascending
   * timestamp order.
   *
   * @param indexPattern index name or wildcard pattern to search
   * @return matching events ordered oldest first; an empty list if none match
   * @throws OpenSearchQueryException if the OpenSearch query fails
   */
  private List<AbstractAuditPOJO> searchAllEvents(String indexPattern) {
    SearchRequest searchRequest =
        SearchRequest.of(
            s ->
                s.index(indexPattern)
                    .ignoreUnavailable(true)
                    .allowNoIndices(true)
                    .sort(so -> so.field(f -> f.field(TIMESTAMP_FIELD).order(SortOrder.Asc))));
    return executeSearch(searchRequest);
  }

  /**
   * Runs a search request against OpenSearch and converts each returned hit into an audit event,
   * setting the OpenSearch document id on it.
   *
   * @param searchRequest request to execute
   * @return matching events in the order OpenSearch returned them; an empty list when the response
   *     carries no hits; a hit whose source is null is skipped
   * @throws OpenSearchQueryException if the OpenSearch query fails
   */
  private List<AbstractAuditPOJO> executeSearch(SearchRequest searchRequest) {
    try {
      SearchResponse<AbstractAuditPOJO> response =
          openSearchClient.search(searchRequest, AbstractAuditPOJO.class);
      if (response.hits() == null || response.hits().hits() == null) {
        return new ArrayList<>();
      }
      return response.hits().hits().stream()
          .filter(hit -> hit.source() != null)
          .map(
              hit -> {
                AbstractAuditPOJO event = hit.source();
                event.setEventId(hit.id());
                return event;
              })
          .collect(Collectors.toList());
    } catch (IOException | OpenSearchException e) {
      throw new OpenSearchQueryException(QUERY_FAILED_MSG, e);
    }
  }

  private void deleteIndex(String indexPattern) {
    try {
      openSearchClient.indices().delete(d -> d.index(indexPattern));
    } catch (OpenSearchException e) {
      if (e.status() != 404) {
        throw new OpenSearchQueryException(DELETE_FAILED_MSG, e);
      }
    } catch (IOException e) {
      throw new OpenSearchQueryException(DELETE_FAILED_MSG, e);
    }
  }
}
