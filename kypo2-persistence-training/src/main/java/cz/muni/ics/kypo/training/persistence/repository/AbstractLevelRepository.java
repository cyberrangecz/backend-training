package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;

/**
 * @author Pavel Seda
 */
@Repository
public interface AbstractLevelRepository extends JpaRepository<AbstractLevel, Long>, QuerydslPredicateExecutor<AbstractLevel> {
}
