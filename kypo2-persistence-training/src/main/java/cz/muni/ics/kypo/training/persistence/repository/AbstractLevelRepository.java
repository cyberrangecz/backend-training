package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;

import java.util.List;
import java.util.Optional;

/**
 * The JPA repository interface to manage {@link AbstractLevel} instances.
 */
@Repository
public interface AbstractLevelRepository extends JpaRepository<AbstractLevel, Long>, QuerydslPredicateExecutor<AbstractLevel> {

    /**
     * Find all levels by training definition id.
     *
     * @param trainingDefinitionId the training definition id
     * @return list of {@link AbstractLevel}s associated with {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition}
     */
    List<AbstractLevel> findAllLevelsByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId);

    /**
     * Find first level for particular training definition
     *
     * @param trainingDefinitionId the training definition id
     * @return {@link AbstractLevel}s associated with {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition}
     */
    List<AbstractLevel> findFirstLevelByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId, Pageable pageable);

    /**
     * Find level in definition.
     *
     * @param trainingDefinitionId the training definition id
     * @param levelId              the level id
     * @return {@link AbstractLevel} from {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition} by ids
     */
    Optional<AbstractLevel> findLevelInDefinition(@Param("trainingDefinitionId") Long trainingDefinitionId,
                                                  @Param("levelId") Long levelId);

    /**
     * Find by id including definition optional.
     *
     * @param levelId the level id
     * @return {@link AbstractLevel} with its associated {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition}
     */
    Optional<AbstractLevel> findByIdIncludingDefinition(@Param("levelId") Long levelId);

    /**
     * Gets current max order.
     *
     * @param trainingDefinitionId the training definition id
     * @return the current max order of {@link AbstractLevel} in given {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition}
     */
    Integer getCurrentMaxOrder(@Param("trainingDefinitionId") Long trainingDefinitionId);

    /**
     * Increase level order from given order to the given order.
     *
     * @param fromOrder first level which order will be increased
     * @param toOrder   last level which order will be increased.
     */
    @Modifying
    void increaseOrderOfLevels(@Param("trainingDefinitionId") Long trainingDefinitionId,
                               @Param("fromOrder") Integer fromOrder,
                               @Param("toOrder") Integer toOrder);

    /**
     * Decrease level order from given order to the given order.
     *
     * @param fromOrder first level which order will be decreased
     * @param toOrder   last level which order will be decreased.
     */
    @Modifying
    void decreaseOrderOfLevels(@Param("trainingDefinitionId") Long trainingDefinitionId,
                               @Param("fromOrder") Integer fromOrder,
                               @Param("toOrder") Integer toOrder);
}
