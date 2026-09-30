package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

/**
 * Manages {@link MinimalSolveTimeDetectionEvent} findings, the joined-inheritance subtype of {@code
 * AbstractDetectionEvent} recording that a level was solved faster than it is held to be solvable
 */
public interface MinimalSolveTimeDetectionEventRepository
    extends JpaRepository<MinimalSolveTimeDetectionEvent, Long>,
        QuerydslPredicateExecutor<MinimalSolveTimeDetectionEvent> {

  /**
   * Returns the minimal solve time finding with the given primary key.
   *
   * @param eventId the primary key of the finding
   */
  MinimalSolveTimeDetectionEvent findMinimalSolveTimeEventById(@Param("eventId") Long eventId);

  /**
   * Returns the minimal solve time findings of one cheating detection sweep, in no defined order.
   *
   * @param cheatingDetectionId the cheating detection the returned findings belong to
   */
  List<MinimalSolveTimeDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
