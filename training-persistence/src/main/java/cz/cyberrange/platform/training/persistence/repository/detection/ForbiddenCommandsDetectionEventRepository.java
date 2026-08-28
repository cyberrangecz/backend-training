package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

/**
 * Manages {@link ForbiddenCommandsDetectionEvent} findings, the joined-inheritance subtype of
 * {@code AbstractDetectionEvent} recording that a trainee ran a forbidden console command
 */
public interface ForbiddenCommandsDetectionEventRepository
    extends JpaRepository<ForbiddenCommandsDetectionEvent, Long>,
        QuerydslPredicateExecutor<ForbiddenCommandsDetectionEvent> {

  /**
   * Returns the forbidden commands finding with the given primary key.
   *
   * @param eventId the primary key of the finding
   */
  ForbiddenCommandsDetectionEvent findForbiddenCommandsEventById(@Param("eventId") Long eventId);

  /**
   * Returns the forbidden commands findings of one cheating detection sweep, in no defined order.
   *
   * @param cheatingDetectionId the cheating detection the returned findings belong to
   */
  List<ForbiddenCommandsDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
