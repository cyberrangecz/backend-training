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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CommandEventsService {
  private static final String TIMESTAMP_STR_FIELD = "timestamp_str";
  private static final String QUERY_FAILED_MSG = "OpenSearch query failed.";
  private static final String DELETE_FAILED_MSG = "OpenSearch delete request failed.";
  private static final String INDEX_PATTERN_SANDBOX = "crczp.logs.console.*.sandbox=%s";
  private static final String INDEX_PATTERN_POOL = "crczp.logs.console.pool=%d.*";
  private static final String INDEX_PATTERN_POOL_SANDBOX = "crczp.logs.console.pool=%d.sandbox=%s";

  // actual run logic

  @Value("${opensearch.max-result-window:10000}")
  private int maxResultWindow;

  private final OpenSearchClient openSearchClient;
  private final ObjectMapper objectMapper;

  @Autowired
  public CommandEventsService(
      OpenSearchClient openSearchClient,
      @Qualifier("openSearchObjectMapper") ObjectMapper objectMapper) {
    this.openSearchClient = openSearchClient;
    this.objectMapper = objectMapper;
  }

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
                      .sort(so -> so.field(f -> f.field(TIMESTAMP_STR_FIELD).order(SortOrder.Asc)))
                      .size(maxResultWindow),
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
