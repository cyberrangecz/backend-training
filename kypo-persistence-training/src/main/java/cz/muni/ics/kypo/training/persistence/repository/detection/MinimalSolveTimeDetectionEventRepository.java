package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MinimalSolveTimeDetectionEventRepository extends JpaRepository<MinimalSolveTimeDetectionEvent, Long>, QuerydslPredicateExecutor<MinimalSolveTimeDetectionEvent> {
}
