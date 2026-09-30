package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/** Manages {@link ForbiddenCommand} rows, declaring no query beyond the inherited ones */
public interface ForbiddenCommandRepository
    extends JpaRepository<ForbiddenCommand, Long>, QuerydslPredicateExecutor<ForbiddenCommand> {}
