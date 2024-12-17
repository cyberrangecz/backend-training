package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;

import java.util.List;

/**
 * The JPA repository interface to manage {@link AssessmentLevel} instances.
 */
@Repository
public interface AssessmentLevelRepository extends JpaRepository<AssessmentLevel, Long>, QuerydslPredicateExecutor<AssessmentLevel> {

    List<AssessmentLevel> findAllByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId);
}
