package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.AbstractDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface AnswerSimilarityDetectionEventRepository extends JpaRepository<AnswerSimilarityDetectionEvent, Long>, QuerydslPredicateExecutor<AnswerSimilarityDetectionEvent> {
}
