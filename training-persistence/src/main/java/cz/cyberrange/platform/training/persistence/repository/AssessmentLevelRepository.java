package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The JPA repository interface to manage {@link AssessmentLevel} instances.
 */
@Repository
public interface AssessmentLevelRepository extends JpaRepository<AssessmentLevel, Long>, QuerydslPredicateExecutor<AssessmentLevel> {

    List<AssessmentLevel> findAllByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId);

    @Query("SELECT al FROM AssessmentLevel al LEFT JOIN FETCH al.questions WHERE al.id = :id")
    Optional<AssessmentLevel> findByIdWithQuestions(@Param("id") Long id);
}
