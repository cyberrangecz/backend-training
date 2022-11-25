package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ForbiddenCommandsDetectionEventRepository extends JpaRepository<ForbiddenCommandsDetectionEvent, Long>, QuerydslPredicateExecutor<ForbiddenCommandsDetectionEvent> {
}
