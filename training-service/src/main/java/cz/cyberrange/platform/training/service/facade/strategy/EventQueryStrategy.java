package cz.cyberrange.platform.training.service.facade.strategy;

import cz.cyberrange.platform.training.opensearch.events.commands.model.TrainingCommand;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import java.util.List;

/**
 * Strategy for querying training events. Implementations differ in which access restrictions they
 * apply — an organizer sees all events, a trainee sees only their own answer events and their own
 * sandbox commands.
 *
 * <p>All restriction logic is delegated to {@link
 * cz.cyberrange.platform.training.service.services.TrainingEventAccessService}; the strategy's sole
 * responsibility is to carry the caller context (null vs. concrete userRefId) into that service.
 */
public interface EventQueryStrategy {

  /**
   * Fetches training audit events for the given instance and event type.
   *
   * @param instanceId training instance id
   * @param eventType OpenSearch type discriminator string (e.g. {@code "level_started"})
   * @param sinceTimestampMs epoch ms lower bound (exclusive)
   * @return matching events, never null
   */
  List<AbstractAuditPOJO> fetchTrainingEvents(
      Long instanceId, String eventType, long sinceTimestampMs);

  /**
   * Fetches console command events for the given instance and pool.
   *
   * @param instanceId training instance id — used by trainee strategy to resolve sandbox
   * @param poolId pool id
   * @param sinceTimestampMs epoch ms lower bound (exclusive)
   * @return matching commands, never null
   */
  List<TrainingCommand> fetchCommandEvents(Long instanceId, Long poolId, long sinceTimestampMs);
}
