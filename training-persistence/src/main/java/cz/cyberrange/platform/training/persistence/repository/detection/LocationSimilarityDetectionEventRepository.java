package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

/**
 * Manages {@link LocationSimilarityDetectionEvent} findings, the joined-inheritance subtype of
 * {@code AbstractDetectionEvent} recording that several trainees submitted from the same network
 * location.
 */
public interface LocationSimilarityDetectionEventRepository
    extends JpaRepository<LocationSimilarityDetectionEvent, Long>,
        QuerydslPredicateExecutor<LocationSimilarityDetectionEvent> {

  /**
   * Returns the location similarity finding with the given primary key.
   *
   * @param eventId the primary key of the finding
   */
  LocationSimilarityDetectionEvent findLocationSimilarityEventById(@Param("eventId") Long eventId);

  /**
   * Returns the location similarity findings of one cheating detection sweep, in no defined
   * order.
   *
   * @param cheatingDetectionId the cheating detection the returned findings belong to
   */
  List<LocationSimilarityDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
