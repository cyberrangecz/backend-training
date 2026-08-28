package cz.cyberrange.platform.training.persistence.repository.detection;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

/**
 * Declares the Querydsl-backed lookup that {@code AbstractDetectionEventRepositoryImpl} carries out
 * against {@link AbstractDetectionEvent}
 */
public interface AbstractDetectionEventRepositoryCustom {

  /**
   * Returns, as one page of distinct rows, the detection events of one cheating detection sweep
   * that also satisfy the given predicate.
   *
   * @param cheatingDetectionId the cheating detection the returned events belong to
   * @param pageable the page to return; a null value defaults to the first page of 20 rows
   * @param predicate an extra condition ANDed onto the cheating detection filter, or null
   */
  Page<AbstractDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId,
      @Param("pageable") Pageable pageable,
      Predicate predicate);
}
