package cz.cyberrange.platform.training.service.facade.strategy;

import cz.cyberrange.platform.training.opensearch.events.commands.model.TrainingCommand;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.service.services.TrainingEventAccessService;
import java.util.List;

/**
 * {@link EventQueryStrategy} for trainees.
 *
 * <p>Passes the caller's {@code userRefId} to {@link TrainingEventAccessService}, which applies
 * per-user restrictions at query level:
 *
 * <ul>
 *   <li>Answer events ({@code CorrectAnswerSubmitted}, {@code WrongAnswerSubmitted}, {@code
 *       AssessmentAnswers}) are filtered to this user's own submissions only.
 *   <li>Console commands are restricted to the trainee's own sandbox index.
 * </ul>
 *
 * <p>This strategy is instantiated per request (not a Spring bean) because it carries
 * caller-specific state. Create it via {@link
 * TraineeEventQueryStrategy#TraineeEventQueryStrategy(TrainingEventAccessService, Long)}.
 */
public class TraineeEventQueryStrategy implements EventQueryStrategy {

  private final TrainingEventAccessService trainingEventAccessService;
  private final Long currentUserRefId;

  /**
   * @param trainingEventAccessService service that enforces event-fetch access restrictions
   * @param currentUserRefId user ref id of the authenticated trainee
   */
  public TraineeEventQueryStrategy(
      TrainingEventAccessService trainingEventAccessService, Long currentUserRefId) {
    this.trainingEventAccessService = trainingEventAccessService;
    this.currentUserRefId = currentUserRefId;
  }

  @Override
  public List<AbstractAuditPOJO> fetchTrainingEvents(
      Long instanceId, String eventType, long sinceTimestampMs) {
    return trainingEventAccessService.fetchTrainingEventsWithRestrictions(
        instanceId, eventType, sinceTimestampMs, currentUserRefId);
  }

  @Override
  public List<TrainingCommand> fetchCommandEvents(
      Long instanceId, Long poolId, long sinceTimestampMs) {
    return trainingEventAccessService.fetchCommandEventsWithRestrictions(
        instanceId, poolId, sinceTimestampMs, currentUserRefId);
  }
}
