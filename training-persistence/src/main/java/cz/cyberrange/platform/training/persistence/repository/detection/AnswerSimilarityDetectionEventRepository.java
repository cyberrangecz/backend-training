package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

/**
 * Manages {@link AnswerSimilarityDetectionEvent} findings, the joined-inheritance subtype of
 * {@code AbstractDetectionEvent} recording that a submitted answer matched another trainee's.
 */
public interface AnswerSimilarityDetectionEventRepository
    extends JpaRepository<AnswerSimilarityDetectionEvent, Long>,
        QuerydslPredicateExecutor<AnswerSimilarityDetectionEvent> {

  /**
   * Returns the answer similarity finding with the given primary key.
   *
   * @param eventId the primary key of the finding
   */
  AnswerSimilarityDetectionEvent findAnswerSimilarityEventById(@Param("eventId") Long eventId);

  /**
   * Returns the answer similarity findings of one cheating detection sweep, in no defined order.
   *
   * @param cheatingDetectionId the cheating detection the returned findings belong to
   */
  List<AnswerSimilarityDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
