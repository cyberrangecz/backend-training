package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.opensearch.events.commands.model.TrainingCommand;
import cz.cyberrange.platform.training.opensearch.events.commands.query.CommandEventsService;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.opensearch.events.training.model.AssessmentAnswered;
import cz.cyberrange.platform.training.opensearch.events.training.model.CorrectAnswerSubmitted;
import cz.cyberrange.platform.training.opensearch.events.training.model.WrongAnswerSubmitted;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingEventsService;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Enforces all event-fetch access restrictions.
 *
 * <p>This service is the single place that knows:
 *
 * <ul>
 *   <li>Which event types carry per-user answer data that trainees must not see across peers.
 *   <li>How to resolve a trainee's sandbox so command queries are scoped to their own index.
 * </ul>
 *
 * <p>Infrastructure query services ({@link TrainingEventsService}, {@link CommandEventsService})
 * remain unaware of access policy; they receive concrete query parameters from this service.
 */
@Service
public class TrainingEventAccessService {

  /** Event types that expose per-user answer content — restricted to the owner in trainee mode */
  static final Set<String> ANSWER_EVENT_TYPES =
      Set.of(CorrectAnswerSubmitted.TYPE, WrongAnswerSubmitted.TYPE, AssessmentAnswered.TYPE);

  private final TrainingEventsService trainingEventsService;
  private final CommandEventsService commandEventsService;
  private final TrainingRunService trainingRunService;
  private final TrainingInstanceService trainingInstanceService;

  /**
   * Creates the service with the event query services and the run/instance services it consults to
   * enforce access policy
   */
  @Autowired
  public TrainingEventAccessService(
      TrainingEventsService trainingEventsService,
      CommandEventsService commandEventsService,
      TrainingRunService trainingRunService,
      TrainingInstanceService trainingInstanceService) {
    this.trainingEventsService = trainingEventsService;
    this.commandEventsService = commandEventsService;
    this.trainingRunService = trainingRunService;
    this.trainingInstanceService = trainingInstanceService;
  }

  /**
   * Fetches training events for the given instance and event type, applying a per-user restriction
   * when the caller is operating in trainee mode.
   *
   * <p>The user restriction is applied only when {@code restrictAnswersToUserRefId} is non-null AND
   * the requested {@code eventType} is one of the answer event types. For all other event types the
   * restriction has no effect — the result is the same regardless of caller.
   *
   * @param instanceId training instance id
   * @param eventType OpenSearch type discriminator string
   * @param sinceTimestampMs epoch ms lower bound (exclusive)
   * @param restrictAnswersToUserRefId when non-null, restricts answer events to this user; pass
   *     null for organizer/admin callers
   * @return matching events, never null
   */
  public List<AbstractAuditPOJO> fetchTrainingEventsWithRestrictions(
      Long instanceId, String eventType, long sinceTimestampMs, Long restrictAnswersToUserRefId) {

    boolean isAnswerType = ANSWER_EVENT_TYPES.contains(eventType);
    Long userRefIdForQuery =
        (isAnswerType && restrictAnswersToUserRefId != null) ? restrictAnswersToUserRefId : null;

    return trainingEventsService.findFilteredTrainingEvents(
        instanceId, eventType, sinceTimestampMs, userRefIdForQuery);
  }

  /**
   * Fetches console command events, restricting to the caller's own sandbox when operating in
   * trainee mode.
   *
   * <p>Commands are stored per-sandbox in separate indices under the pool holding the instance, so
   * the pool is resolved from the instance itself. Restriction is achieved by targeting the
   * trainee's specific sandbox index rather than the pool-wide wildcard, avoiding any post-fetch
   * filtering overhead.
   *
   * <p>Either scoping key may fail to resolve — an instance holds no pool, or a restricted caller
   * has no sandbox — and each yields no commands rather than a wider query.
   *
   * @param instanceId training instance id — resolves both the pool and the trainee's sandbox
   * @param sinceTimestampMs epoch ms lower bound (exclusive)
   * @param restrictToUserRefId when non-null, resolves the trainee's sandbox and queries only that
   *     index; when null, queries all sandboxes in the pool
   * @return matching commands, never null; empty when the instance holds no pool, or when a
   *     restricted caller has no resolvable sandbox in it
   * @throws EntityNotFoundException if {@code instanceId} does not resolve to a training instance
   */
  public List<TrainingCommand> fetchCommandEventsWithRestrictions(
      Long instanceId, long sinceTimestampMs, Long restrictToUserRefId) {

    Long poolId = trainingInstanceService.findById(instanceId).getPoolId();
    if (poolId == null) {
      return List.of();
    }

    if (restrictToUserRefId == null) {
      return commandEventsService.findFilteredCommandEvents(poolId, sinceTimestampMs, null);
    }

    String traineeSandboxId = resolveTraineeSandboxId(instanceId, restrictToUserRefId);
    return traineeSandboxId == null
        ? List.of()
        : commandEventsService.findFilteredCommandEvents(
            poolId, sinceTimestampMs, traineeSandboxId);
  }

  /**
   * Resolves the sandbox UUID for a trainee's training run within the given instance. For archived
   * runs whose active sandbox has been released, falls back to the previous sandbox identifier.
   *
   * @param instanceId training instance id
   * @param userRefId participant user ref id
   * @return the sandbox UUID of the participant's run, falling back to the previous sandbox
   *     identifier for archived runs whose active sandbox was released; {@code null} when no
   *     matching run exists or no sandbox was ever assigned
   */
  public String resolveTraineeSandboxId(Long instanceId, Long userRefId) {
    return trainingRunService.findAllByTrainingInstanceId(instanceId).stream()
        .filter(run -> run.getParticipantRef().getUserRefId().equals(userRefId))
        .findFirst()
        .map(
            run ->
                run.getSandboxInstanceRefId() != null
                    ? run.getSandboxInstanceRefId()
                    : run.getPreviousSandboxInstanceRefId())
        .orElse(null);
  }
}
