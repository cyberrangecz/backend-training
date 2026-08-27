package cz.cyberrange.platform.training.opensearch.events.commands.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.cyberrange.platform.training.opensearch.events.commands.model.TrainingCommand;
import cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions.OpenSearchQueryException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CommandEventsService {
  private static final String TIMESTAMP_STR_FIELD = "timestamp_str";
  private static final String QUERY_FAILED_MSG = "OpenSearch query failed.";
  private static final String DELETE_FAILED_MSG = "OpenSearch delete request failed.";
  private static final String INDEX_PATTERN_SANDBOX = "crczp.logs.console.*.sandbox=%s";
  private static final String INDEX_PATTERN_POOL = "crczp.logs.console.pool=%d.*";
  private static final String INDEX_PATTERN_POOL_SANDBOX = "crczp.logs.console.pool=%d.sandbox=%s";

  private final OpenSearchClient openSearchClient;
  private final ObjectMapper objectMapper;

  @Autowired
  public CommandEventsService(
      OpenSearchClient openSearchClient,
      @Qualifier("openSearchObjectMapper") ObjectMapper objectMapper) {
    this.openSearchClient = openSearchClient;
    this.objectMapper = objectMapper;
  }

  /**
   * Retrieves every console command logged for a sandbox, matched by sandbox id alone across every
   * pool the sandbox could belong to.
   *
   * @param sandboxId sandbox id whose commands are fetched
   * @return commands ordered by ascending logged timestamp, oldest first; an empty list if none
   *     match
   * @throws OpenSearchQueryException if the OpenSearch query fails
   */
  public List<TrainingCommand> findAllConsoleCommandsBySandbox(String sandboxId)
      throws OpenSearchQueryException {
    String index = String.format(INDEX_PATTERN_SANDBOX, sandboxId);
    Query query = Query.of(q -> q.matchAll(m -> m));
    return searchAsCommands(index, query);
  }

  /**
   * Checks whether there are any logged commands for a specific sandbox id.
   *
   * @param sandboxId the sandbox id
   * @return true if commands exist, false otherwise
   * @throws OpenSearchQueryException if the OpenSearch query fails
   */
  public boolean hasConsoleCommandsBySandbox(String sandboxId) throws OpenSearchQueryException {
    String index = String.format(INDEX_PATTERN_SANDBOX, sandboxId);
    try {
      SearchResponse<ObjectNode> response =
          openSearchClient.search(
              s -> s.index(index).ignoreUnavailable(true).allowNoIndices(true).size(0),
              ObjectNode.class);
      return response.hits().total() != null && response.hits().total().value() > 0;
    } catch (OpenSearchException | IOException e) {
      throw new OpenSearchQueryException(QUERY_FAILED_MSG, e);
    }
  }

  /**
   * Retrieves console commands logged for a sandbox whose logged timestamp string falls within the
   * given bounds, matched by sandbox id alone across every pool the sandbox could belong to.
   *
   * @param sandboxId sandbox id whose commands are fetched
   * @param from lower bound on the logged timestamp string, inclusive
   * @param to upper bound on the logged timestamp string, inclusive
   * @return commands ordered by ascending logged timestamp, oldest first; an empty list if none
   *     match
   * @throws OpenSearchQueryException if the OpenSearch query fails
   */
  public List<TrainingCommand> findAllConsoleCommandsBySandboxAndTimeRange(
      String sandboxId, Long from, Long to) throws OpenSearchQueryException {
    String index = String.format(INDEX_PATTERN_SANDBOX, sandboxId);
    Query query =
        Query.of(
            q ->
                q.range(
                    r -> r.field(TIMESTAMP_STR_FIELD).gte(JsonData.of(from)).lte(JsonData.of(to))));
    return searchAsCommands(index, query);
  }

  /**
   * Fetches console commands filtered by timestamp. When {@code sandboxId} is provided, only
   * commands from that specific sandbox index are returned — used to restrict trainees to their own
   * sandbox. When {@code sandboxId} is null, all sandboxes for the pool are queried.
   *
   * <p>Restriction is achieved by index targeting, not by a field filter — each sandbox writes to
   * its own index ({@code crczp.logs.console.pool={poolId}.sandbox={sandboxId}}), so narrowing the
   * index is equivalent to restricting by ownership.
   *
   * @param poolId pool id; always required to scope the query
   * @param sinceTimestampMs epoch milliseconds lower bound (exclusive); only commands with {@code
   *     timestamp_str > sinceTimestampMs} are returned
   * @param sandboxId when non-null, queries only that sandbox's index (trainee mode); when null,
   *     queries all sandboxes in the pool (organizer mode)
   * @return list of matching commands, never null
   */
  public List<TrainingCommand> findFilteredCommandEvents(
      Long poolId, long sinceTimestampMs, String sandboxId) {
    String targetIndex =
        sandboxId != null
            ? String.format(INDEX_PATTERN_POOL_SANDBOX, poolId, sandboxId)
            : String.format(INDEX_PATTERN_POOL, poolId);

    Query mustBeAfterTimestamp =
        Query.of(
            clause ->
                clause.range(
                    range -> range.field(TIMESTAMP_STR_FIELD).gt(JsonData.of(sinceTimestampMs))));

    return searchAsCommands(targetIndex, mustBeAfterTimestamp);
  }

  public void deleteCommandsBySandbox(String sandboxId) throws OpenSearchQueryException {
    deleteConsoleCommands(String.format(INDEX_PATTERN_SANDBOX, sandboxId));
  }

  public void deleteCommandsByPool(Long poolId) throws OpenSearchQueryException {
    deleteConsoleCommands(String.format(INDEX_PATTERN_POOL, poolId));
  }

  private void deleteConsoleCommands(String index) throws OpenSearchQueryException {
    try {
      openSearchClient.indices().delete(d -> d.index(index));
    } catch (OpenSearchException e) {
      if (e.status() == 404) return;
      throw new OpenSearchQueryException(DELETE_FAILED_MSG, e);
    } catch (IOException e) {
      throw new OpenSearchQueryException(DELETE_FAILED_MSG, e);
    }
  }

  /**
   * Runs a search request against OpenSearch and converts each returned hit into a {@link
   * TrainingCommand}, in ascending logged-timestamp order, setting the OpenSearch document id on
   * each command.
   *
   * @param index index name or wildcard pattern to search
   * @param query query the search request is restricted by
   * @return matching commands ordered oldest first; an empty list when the response carries no
   *     hits; a hit whose source is null is skipped
   * @throws OpenSearchQueryException if the OpenSearch query fails
   */
  private List<TrainingCommand> searchAsCommands(String index, Query query)
      throws OpenSearchQueryException {
    try {
      SearchResponse<ObjectNode> response =
          openSearchClient.search(
              s ->
                  s.index(index)
                      .ignoreUnavailable(true)
                      .allowNoIndices(true)
                      .query(query)
                      .sort(so -> so.field(f -> f.field(TIMESTAMP_STR_FIELD).order(SortOrder.Asc))),
              ObjectNode.class);
      List<TrainingCommand> commands = new ArrayList<>();
      if (response.hits() != null && response.hits().hits() != null) {
        for (Hit<ObjectNode> hit : response.hits().hits()) {
          if (hit.source() != null) {
            TrainingCommand command =
                objectMapper.convertValue(hit.source(), TrainingCommand.class);
            command.setEventId(hit.id());
            commands.add(command);
          }
        }
      }
      return commands;
    } catch (OpenSearchException | IOException e) {
      throw new OpenSearchQueryException(QUERY_FAILED_MSG, e);
    }
  }
}
