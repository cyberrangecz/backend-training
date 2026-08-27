package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link AbstractLevel} instances. */
@Repository
public interface AbstractLevelRepository
    extends JpaRepository<AbstractLevel, Long>, QuerydslPredicateExecutor<AbstractLevel> {

  /**
   * Find all levels by training definition id, ordered by level order within the definition.
   *
   * @param trainingDefinitionId the training definition id
   * @return list of {@link AbstractLevel}s associated with {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition}
   */
  List<AbstractLevel> findAllLevelsByTrainingDefinitionId(
      @Param("trainingDefinitionId") Long trainingDefinitionId);

  /**
   * Find all levels belonging to any of the given training definitions, ordered by level order;
   * levels from different definitions are interleaved by that order rather than grouped by
   * definition.
   *
   * @param trainingDefinitionIds the training definition ids
   * @return list of {@link AbstractLevel}s associated with any of the given {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition}s
   */
  List<AbstractLevel> findAllLevelsByTrainingDefinitionIdIn(
      @Param("trainingDefinitionIds") Collection<Long> trainingDefinitionIds);

  /**
   * Find all levels by level ids, confined to one training definition.
   *
   * @param levelIds the ids of the levels
   * @param trainingDefinitionId the training definition the levels have to belong to
   * @return list of {@link AbstractLevel}s carrying one of the given ids and belonging to that
   *     definition, a level of another definition being left out without notice
   */
  List<AbstractLevel> findAllByIdIsInAndTrainingDefinitionId(
      List<Long> levelIds, Long trainingDefinitionId);

  /**
   * Find first level for particular training definition.
   *
   * @param trainingDefinitionId the training definition id
   * @return {@link AbstractLevel}s associated with {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition}
   */
  List<AbstractLevel> findFirstLevelByTrainingDefinitionId(
      @Param("trainingDefinitionId") Long trainingDefinitionId, Pageable pageable);

  /**
   * Find level in definition.
   *
   * @param trainingDefinitionId the training definition id
   * @param levelId the level id
   * @return {@link AbstractLevel} from {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition} by ids
   */
  Optional<AbstractLevel> findLevelInDefinition(
      @Param("trainingDefinitionId") Long trainingDefinitionId, @Param("levelId") Long levelId);

  /**
   * Find a level by id, with its training definition, that definition's authors and, when present,
   * its beta testing group's organizers loaded eagerly along with it.
   *
   * @param levelId the level id
   * @return {@link AbstractLevel} with its associated {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition}
   */
  Optional<AbstractLevel> findByIdIncludingDefinition(@Param("levelId") Long levelId);

  /**
   * Gets the highest level order value used within the given training definition.
   *
   * @param trainingDefinitionId the training definition id
   * @return the current max order of {@link AbstractLevel} in given {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition}, or -1 when the
   *     definition has no levels
   */
  Integer getCurrentMaxOrder(@Param("trainingDefinitionId") Long trainingDefinitionId);

  /**
   * Adds 1 to the order of every level of the given training definition whose order lies between
   * the given bounds, both inclusive.
   *
   * @param trainingDefinitionId the training definition whose levels are affected
   * @param fromOrder lowest order value to increase
   * @param toOrder highest order value to increase
   */
  @Modifying
  void increaseOrderOfLevels(
      @Param("trainingDefinitionId") Long trainingDefinitionId,
      @Param("fromOrder") Integer fromOrder,
      @Param("toOrder") Integer toOrder);

  /**
   * Subtracts 1 from the order of every level of the given training definition whose order lies
   * between the given bounds, both inclusive.
   *
   * @param trainingDefinitionId the training definition whose levels are affected
   * @param fromOrder lowest order value to decrease
   * @param toOrder highest order value to decrease
   */
  @Modifying
  void decreaseOrderOfLevels(
      @Param("trainingDefinitionId") Long trainingDefinitionId,
      @Param("fromOrder") Integer fromOrder,
      @Param("toOrder") Integer toOrder);

  List<AbstractLevel> findAllByIdIn(Collection<Long> ids);
}
