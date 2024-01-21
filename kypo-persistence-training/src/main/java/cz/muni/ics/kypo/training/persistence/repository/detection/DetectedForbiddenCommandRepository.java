package cz.muni.ics.kypo.training.persistence.repository.detection;

import cz.muni.ics.kypo.training.persistence.model.detection.DetectedForbiddenCommand;
import cz.muni.ics.kypo.training.persistence.model.detection.ForbiddenCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface DetectedForbiddenCommandRepository extends JpaRepository<DetectedForbiddenCommand, Long>, QuerydslPredicateExecutor<DetectedForbiddenCommand> {
}
