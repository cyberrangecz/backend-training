package cz.cyberrange.platform.training.service.facade.strategy;

import cz.cyberrange.platform.training.opensearch.events.commands.model.TrainingCommand;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.service.services.TrainingEventAccessService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link EventQueryStrategy} for organizers and administrators.
 *
 * <p>Passes no user restriction to {@link TrainingEventAccessService} — full event data is returned
 * for all participants. This strategy is a singleton Spring component injected wherever
 * organizer-level access is needed.
 */
@Component
public class OrganizerEventQueryStrategy implements EventQueryStrategy {

  private final TrainingEventAccessService trainingEventAccessService;

  /**
   * @param trainingEventAccessService service that enforces event-fetch access restrictions
   */
  @Autowired
  public OrganizerEventQueryStrategy(TrainingEventAccessService trainingEventAccessService) {
    this.trainingEventAccessService = trainingEventAccessService;
  }

  @Override
  public List<AbstractAuditPOJO> fetchTrainingEvents(
      Long instanceId, String eventType, long sinceTimestampMs) {
    return trainingEventAccessService.fetchTrainingEventsWithRestrictions(
        instanceId, eventType, sinceTimestampMs, null);
  }

  @Override
  public List<TrainingCommand> fetchCommandEvents(
      Long instanceId, Long poolId, long sinceTimestampMs) {
    return trainingEventAccessService.fetchCommandEventsWithRestrictions(
        instanceId, poolId, sinceTimestampMs, null);
  }
}
