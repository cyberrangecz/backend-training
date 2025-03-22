package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.JeopardyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The JPA repository interface to manage {@link JeopardyLevel} instances.
 */
@Repository
public interface JeopardyLevelRepository extends JpaRepository<JeopardyLevel, Long>, QuerydslPredicateExecutor<JeopardyLevel> {

    List<JeopardyLevel> findAllByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId);
}