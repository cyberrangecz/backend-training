package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.AccessLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * The JPA repository interface to manage {@link AccessLevel} instances.
 */
@Repository
public interface AccessLevelRepository extends JpaRepository<AccessLevel, Long>, QuerydslPredicateExecutor<AccessLevel> {

}
