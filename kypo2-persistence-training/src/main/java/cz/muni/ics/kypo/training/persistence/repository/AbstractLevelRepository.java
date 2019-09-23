package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;

import java.util.List;
import java.util.Optional;

/**
 * The JPA repository interface to manage {@link AbstractLevel} instances.
 *
 * @author Pavel Seda
 */
@Repository
public interface AbstractLevelRepository extends JpaRepository<AbstractLevel, Long>, QuerydslPredicateExecutor<AbstractLevel> {

    /**
     * Find all levels by training definition id.
     *
     * @param trainingDefinitionId the training definition id
     * @return list of {@link AbstractLevel}s associated with {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition}
     */
    @Query("SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId ORDER BY l.order")
    List<AbstractLevel> findAllLevelsByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId);

    /**
     * Gets current max order.
     *
     * @param trainingDefinitionId the training definition id
     * @return the current max order of {@link AbstractLevel} in given {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition}
     */
    @Query("SELECT COALESCE(MAX(l.order), -1) FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId")
    Integer getCurrentMaxOrder(@Param("trainingDefinitionId") Long trainingDefinitionId);

    /**
     * Find level in definition.
     *
     * @param trainingDefinitionId the training definition id
     * @param levelId              the level id
     * @return {@link AbstractLevel} from {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition} by ids
     */
    @Query("SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.id = :levelId")
    Optional<AbstractLevel> findLevelInDefinition(@Param("trainingDefinitionId") Long trainingDefinitionId, @Param("levelId") Long levelId);

    /**
     * Find by id including definition optional.
     *
     * @param levelId the level id
     * @return {@link AbstractLevel} with its associated {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition}
     */
    @Query("SELECT l FROM AbstractLevel l JOIN FETCH l.trainingDefinition td JOIN FETCH td.authors LEFT OUTER JOIN FETCH td.betaTestingGroup btg LEFT OUTER JOIN FETCH btg.organizers WHERE l.id = :levelId")
    Optional<AbstractLevel> findByIdIncludinDefinition(@Param("levelId") Long levelId);

    /**
     * Increase level order from given order to the given order.
     *
     * @param fromOrder first level which order will be increased
     * @param toOrder last level which order will be increased.
     */
    @Modifying
    @Query("UPDATE AbstractLevel l SET l.order = l.order + 1 WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.order >= :fromOrder AND l.order <= :toOrder")
    void increaseOrderOfLevels(@Param("trainingDefinitionId") Long trainingDefinitionId, @Param("fromOrder") Integer fromOrder, @Param("toOrder") Integer toOrder);

    /**
     * Decrease level order from given order to the given order.
     *
     * @param fromOrder first level which order will be decreased
     * @param toOrder last level which order will be decreased.
     */
    @Modifying
    @Query("UPDATE AbstractLevel l SET l.order = l.order - 1 WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.order >= :fromOrder AND l.order <= :toOrder")
    void decreaseOrderOfLevels(@Param("trainingDefinitionId") Long trainingDefinitionId, @Param("fromOrder") Integer fromOrder, @Param("toOrder") Integer toOrder);
}
