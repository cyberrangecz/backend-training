package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link TrainingLevel} instances. */
@Repository
public interface TrainingLevelRepository
    extends JpaRepository<TrainingLevel, Long>, QuerydslPredicateExecutor<TrainingLevel> {

  List<TrainingLevel> findAllByTrainingDefinitionId(
      @Param("trainingDefinitionId") Long trainingDefinitionId);

  Collection<TrainingLevel> findAllByTrainingDefinitionIdIn(
      Set<Long> participatedTrainingDefinitions);
}
