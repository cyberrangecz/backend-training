package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.NoCommandsDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

/**
 * Manages {@link NoCommandsDetectionEvent} findings, the joined-inheritance subtype of
 * {@code AbstractDetectionEvent} recording that a level requiring console commands was solved
 * without any command being recorded for it.
 */
public interface NoCommandsDetectionEventRepository
    extends JpaRepository<NoCommandsDetectionEvent, Long>,
        QuerydslPredicateExecutor<NoCommandsDetectionEvent> {

  /**
   * Returns the no-commands finding with the given primary key.
   *
   * @param eventId the primary key of the finding
   */
  NoCommandsDetectionEvent findNoCommandsEventById(@Param("eventId") Long eventId);

  /**
   * Returns the no-commands findings of one cheating detection sweep, in no defined order.
   *
   * @param cheatingDetectionId the cheating detection the returned findings belong to
   */
  List<NoCommandsDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
