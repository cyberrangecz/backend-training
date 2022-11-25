package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.DetectionEventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface DetectionEventParticipantRepository extends JpaRepository<DetectionEventParticipant, Long>, QuerydslPredicateExecutor<DetectionEventParticipant> {
}
