package cz.muni.ics.kypo.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.muni.ics.kypo.training.persistence.model.QTrainingRun;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * The JPA repository interface to manage {@link TrainingRun} instances.
 *
 * @author Pavel Seda (441048)
 */
@Repository
public interface TrainingRunRepository extends JpaRepository<TrainingRun, Long>, QuerydslPredicateExecutor<TrainingRun>, QuerydslBinderCustomizer<QTrainingRun> {

    /**
     * That method is used to make the query dsl string values case insensitive and also it supports partial matches in the database.
     *
     * @param querydslBindings
     * @param qTrainingRun
     */
    @Override
    default void customize(QuerydslBindings querydslBindings, QTrainingRun qTrainingRun) {
        querydslBindings.bind(String.class).all((StringPath path, Collection<? extends String> values) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
            return Optional.ofNullable(predicate);
        });
    }

    /**
     * Find all training runs
     *
     * @param predicate the predicate
     * @param pageable  the pageable
     * @return page of all {@link TrainingRun}
     */
    @EntityGraph(attributePaths = {"participantRef"})
    Page<TrainingRun> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find training run by id
     *
     * @param id id of training run
     * @return {@link TrainingRun}
     */
    @EntityGraph(attributePaths = {"participantRef", "trainingInstance"})
    Optional<TrainingRun> findById(Long id);

    /**
     * Find all training runs accessed by participant by their user ref id.
     *
     * @param userRefId the participant ref id
     * @param pageable  the pageable
     * @return the page of all {@link TrainingRun}s accessed by participant
     */
    @Query(value = "SELECT tr FROM TrainingRun tr JOIN FETCH tr.participantRef pr JOIN FETCH tr.trainingInstance ti " +
            "JOIN FETCH ti.trainingDefinition WHERE pr.userRefId = :userRefId",
            countQuery = "SELECT COUNT(tr) FROM TrainingRun tr INNER JOIN tr.participantRef pr INNER JOIN tr.trainingInstance ti " +
                    "INNER JOIN ti.trainingDefinition WHERE pr.userRefId = :userRefId")
    Page<TrainingRun> findAllByParticipantRefId(@Param("userRefId") Long userRefId, Pageable pageable);

    /**
     * Find training run by id including current level
     *
     * @param trainingRunId the training run id
     * @return {@link TrainingRun} including {@link cz.muni.ics.kypo.training.persistence.model.AbstractLevel}
     */
    @Query("SELECT tr FROM TrainingRun tr JOIN FETCH tr.currentLevel JOIN FETCH tr.trainingInstance ti JOIN FETCH ti.trainingDefinition WHERE tr.id= :trainingRunId")
    Optional<TrainingRun> findByIdWithLevel(@Param("trainingRunId") Long trainingRunId);

    /**
     * Find all training runs by id of associated training definition that are accessible to participant by user ref id.
     *
     * @param trainingDefinitionId the training definition id
     * @param userRefId            the participant user ref id
     * @param pageable             the pageable
     * @return the page of all {@link TrainingRun}s by id of associated {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition} that are accessible to participant
     */
    @Query(value = "SELECT tr FROM TrainingRun tr JOIN FETCH tr.participantRef pr JOIN FETCH tr.trainingInstance ti JOIN FETCH "
            + "ti.trainingDefinition td WHERE td.id = :trainingDefinitionId AND pr.userRefId = :userRefId",
            countQuery = "SELECT COUNT(tr) FROM TrainingRun tr INNER JOIN tr.participantRef pr INNER JOIN tr.trainingInstance ti INNER JOIN " +
                    "ti.trainingDefinition td WHERE td.id = :trainingDefinitionId AND pr.userRefId = :userRefId")
    Page<TrainingRun> findAllByTrainingDefinitionIdAndParticipantUserRefId(@Param("trainingDefinitionId") Long trainingDefinitionId,
                                                                           @Param("userRefId") Long userRefId, Pageable pageable);

    /**
     * Find all training runs associated with training instance.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all {@link TrainingRun}s associated with {@link TrainingInstance}
     */
    @EntityGraph(attributePaths = {"participantRef"})
    Page<TrainingRun> findAllByTrainingInstanceId(Long trainingInstanceId, Pageable pageable);

    /**
     * Find all training runs associated with training instance.
     *
     * @param trainingInstanceId the training instance id
     * @return the set of all {@link TrainingRun}s associated with {@link TrainingInstance}
     */
    @EntityGraph(attributePaths = {"participantRef"})
    Set<TrainingRun> findAllByTrainingInstanceId(Long trainingInstanceId);

    /**
     * Find all active training runs by training instance id.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all active {@link TrainingRun}s associated with given {@link TrainingInstance}
     */
    @Query(value = "SELECT tr FROM TrainingRun tr JOIN FETCH tr.trainingInstance ti WHERE ti.id = :trainingInstanceId AND tr.state <> 'ARCHIVED'",
            countQuery = "SELECT COUNT(tr) FROM TrainingRun tr INNER JOIN tr.trainingInstance ti WHERE ti.id = :trainingInstanceId AND tr.state <> 'ARCHIVED'")
    Page<TrainingRun> findAllActiveByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId, Pageable pageable);

    /**
     * Find all inactive training runs by training instance id.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all inactive {@link TrainingRun}s associated with given {@link TrainingInstance}
     */
    @Query(value = "SELECT tr FROM TrainingRun tr JOIN FETCH tr.trainingInstance ti WHERE ti.id = :trainingInstanceId AND tr.state = 'ARCHIVED'",
            countQuery = "SELECT COUNT(tr) FROM TrainingRun tr INNER JOIN tr.trainingInstance ti WHERE ti.id = :trainingInstanceId AND tr.state = 'ARCHIVED'")
    Page<TrainingRun> findAllInactiveByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId, Pageable pageable);

    /**
     * Find all training runs associated with training definition.
     *
     * @param trainingDefinitionId the training definition id
     * @param pageable             the pageable
     * @return the page of all {@link TrainingRun}s associated with {@link cz.muni.ics.kypo.training.persistence.model.TrainingDefinition}
     */
    @Query(value = "SELECT tr FROM TrainingRun tr JOIN FETCH tr.trainingInstance ti JOIN FETCH ti.trainingDefinition td WHERE td.id = :trainingDefinitionId",
            countQuery = "SELECT COUNT(tr) FROM TrainingRun tr INNER JOIN tr.trainingInstance ti INNER JOIN ti.trainingDefinition td WHERE td.id = :trainingDefinitionId")
    Page<TrainingRun> findAllByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId, Pageable pageable);

    /**
     * Delete all training runs by training instance.
     *
     * @param trainingInstanceId the training instance id
     */
    @Modifying
    @Query("DELETE FROM TrainingRun tr WHERE tr.trainingInstance.id = :trainingInstanceId")
    void deleteTrainingRunsByTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

    /**
     * Find valid training run by user and access token.
     *
     * @param accessToken the access token
     * @param userRefId   the user ref id
     * @return the {@link TrainingRun} by user and access token
     */
    @Query("SELECT tr FROM TrainingRun tr JOIN FETCH tr.trainingInstance ti JOIN FETCH tr.participantRef pr JOIN FETCH tr.currentLevel WHERE ti.accessToken = :accessToken " +
            "AND pr.userRefId = :userRefId AND tr.sandboxInstanceRefId IS NOT NULL AND tr.state NOT LIKE 'FINISHED' ")
    Optional<TrainingRun> findValidTrainingRunOfUser(@Param("accessToken") String accessToken, @Param("userRefId") Long userRefId);

//    /**
//     * Find by sandbox instance ref.
//     *
//     * @param sandboxInstanceRef the sandbox instance ref
//     * @return the {@link TrainingRun}
//     */
//    @Query("SELECT tr FROM TrainingRun tr WHERE tr.sandboxInstanceRef = :sandboxInstanceRef")
//    Optional<TrainingRun> findBySandboxInstanceRef(@Param("sandboxInstanceRef") SandboxInstanceRef sandboxInstanceRef);
//
//    @Query("SELECT si.sandboxInstanceRefId FROM TrainingRun tr INNER JOIN tr.sandboxInstanceRef si INNER JOIN tr.trainingInstance ti WHERE ti.id = :trainingInstanceId")
//    List<Long> findIdsOfAllOccupiedSandboxesByTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

    @Query("SELECT (COUNT(tr) > 0) FROM TrainingRun tr INNER JOIN tr.trainingInstance ti WHERE ti.id = :trainingInstanceId")
    boolean existsAnyForTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

    @EntityGraph(attributePaths = {"participantRef"})
    Optional<TrainingRun> findBySandboxInstanceRefId(Long sandboxId);

}
