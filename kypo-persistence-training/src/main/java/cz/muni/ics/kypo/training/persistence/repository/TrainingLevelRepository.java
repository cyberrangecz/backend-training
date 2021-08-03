package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.TrainingLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * The JPA repository interface to manage {@link TrainingLevel} instances.
 */
@Repository
public interface TrainingLevelRepository extends JpaRepository<TrainingLevel, Long>, QuerydslPredicateExecutor<TrainingLevel> {

}
