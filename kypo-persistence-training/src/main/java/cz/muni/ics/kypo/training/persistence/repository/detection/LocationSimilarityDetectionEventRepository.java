package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface LocationSimilarityDetectionEventRepository extends JpaRepository<LocationSimilarityDetectionEvent, Long>, QuerydslPredicateExecutor<LocationSimilarityDetectionEvent> {
}
