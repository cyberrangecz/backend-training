package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.TimeProximityDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

/**
 * Manages {@link TimeProximityDetectionEvent} findings, the joined-inheritance subtype of {@code
 * AbstractDetectionEvent} recording that several trainees solved the same level closer together in
 * time than the sweep's tolerance allows
 */
public interface TimeProximityDetectionEventRepository
    extends JpaRepository<TimeProximityDetectionEvent, Long>,
        QuerydslPredicateExecutor<TimeProximityDetectionEvent> {

  /**
   * Returns the time proximity finding with the given primary key.
   *
   * @param eventId the primary key of the finding
   */
  TimeProximityDetectionEvent findTimeProximityEventById(@Param("eventId") Long eventId);

  /**
   * Returns the time proximity findings of one cheating detection sweep, in no defined order.
   *
   * @param cheatingDetectionId the cheating detection the returned findings belong to
   */
  List<TimeProximityDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
