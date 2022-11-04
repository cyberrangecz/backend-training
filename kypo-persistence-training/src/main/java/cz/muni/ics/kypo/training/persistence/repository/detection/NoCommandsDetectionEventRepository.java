package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.NoCommandsDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface NoCommandsDetectionEventRepository extends JpaRepository<NoCommandsDetectionEvent, Long>, QuerydslPredicateExecutor<NoCommandsDetectionEvent> {
}
