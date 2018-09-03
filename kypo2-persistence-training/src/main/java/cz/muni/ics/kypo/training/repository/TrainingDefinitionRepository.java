package cz.muni.ics.kypo.training.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.model.TrainingDefinition;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Repository
public interface TrainingDefinitionRepository
    extends JpaRepository<TrainingDefinition, Long>, QuerydslPredicateExecutor<TrainingDefinition> {

  @Query("SELECT al.id FROM TrainingDefinition td JOIN AbstractLevel al ON td.id = al.id")
  List<Long> findAllIdsFromAbstractLevel();
}
