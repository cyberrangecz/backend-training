package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.TimeProximityDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TimeProximityDetectionEventRepository extends JpaRepository<TimeProximityDetectionEvent, Long>, QuerydslPredicateExecutor<TimeProximityDetectionEvent> {
}
