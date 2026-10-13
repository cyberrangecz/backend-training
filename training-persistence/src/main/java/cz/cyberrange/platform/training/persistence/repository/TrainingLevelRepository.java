package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link TrainingLevel} instances */
@Repository
public interface TrainingLevelRepository
    extends JpaRepository<TrainingLevel, Long>, QuerydslPredicateExecutor<TrainingLevel> {

  /**
   * Finds the training levels belonging to the given training definition. Ordered by the level
   * order, then by id. Returns an empty list when the definition has none.
   *
   * @param trainingDefinitionId the training definition id
   * @return the matching {@link TrainingLevel}s, associations left lazy
   */
  List<TrainingLevel> findAllByTrainingDefinitionIdOrderByOrderAscIdAsc(
      @Param("trainingDefinitionId") Long trainingDefinitionId);

  /**
   * Finds the training levels belonging to any of the given training definitions. Ordered by id.
   * Returns an empty collection when none match.
   *
   * @param participatedTrainingDefinitions the training definition ids
   * @return the matching {@link TrainingLevel}s, associations left lazy
   */
  Collection<TrainingLevel> findAllByTrainingDefinitionIdInOrderById(
      Set<Long> participatedTrainingDefinitions);
}
