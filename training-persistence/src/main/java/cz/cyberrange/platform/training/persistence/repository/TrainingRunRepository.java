package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.training.persistence.model.QTrainingRun;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The JPA repository interface to manage {@link TrainingRun} instances.
 */
@Repository
public interface TrainingRunRepository extends JpaRepository<TrainingRun, Long>, TrainingRunRepositoryCustom, QuerydslPredicateExecutor<TrainingRun>, QuerydslBinderCustomizer<QTrainingRun> {

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
    @EntityGraph(
            value = "TrainingRun.findAllParticipantRef",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Page<TrainingRun> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find all training runs associated with training instance.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all {@link TrainingRun}s associated with {@link TrainingInstance}
     */
    @Query("SELECT tr FROM TrainingRun tr WHERE tr.trainingInstance.id = :trainingInstanceId ORDER BY tr.startTime ASC")
    @EntityGraph(
            value = "TrainingRun.findAllParticipantRef",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Page<TrainingRun> findAllByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId, Pageable pageable);


    /**
     * Find all training runs associated with training instance.
     *
     * @param trainingInstanceId the training instance id
     * @return the set of all {@link TrainingRun}s associated with {@link TrainingInstance}
     */
    @EntityGraph(
            value = "TrainingRun.findAllParticipantRef",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Set<TrainingRun> findAllByTrainingInstanceId(Long trainingInstanceId);

    /**
     * Find training run by id
     *
     * @param id id of training run
     * @return {@link TrainingRun}
     */
    @EntityGraph(
            value = "TrainingRun.findByIdParticipantRefTrainingInstance",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Optional<TrainingRun> findById(Long id);

    /**
     * Find all training runs accessed by participant by their user ref id.
     *
     * @param userRefId the participant ref id
     * @param pageable  the pageable
     * @return the page of all {@link TrainingRun}s accessed by participant
     */
    Page<TrainingRun> findAllByParticipantRefId(@Param("userRefId") Long userRefId, Pageable pageable);

    /**
     * Find training run by id including current level
     *
     * @param trainingRunId the training run id
     * @return {@link TrainingRun} including {@link cz.cyberrange.platform.training.persistence.model.AbstractLevel}
     */
    Optional<TrainingRun> findByIdWithLevel(@Param("trainingRunId") Long trainingRunId);

    /**
     * Find training run by id including current level, without write lock.
     * Use for resume path only to avoid deadlocks on training_instance when multiple requests run concurrently.
     *
     * @param trainingRunId the training run id
     * @return {@link TrainingRun} including {@link cz.cyberrange.platform.training.persistence.model.AbstractLevel}
     */
    @Query(name = "TrainingRun.findByIdWithLevelForResume")
    Optional<TrainingRun> findByIdWithLevelForResume(@Param("trainingRunId") Long trainingRunId);

    /**
     * Find all training runs by id of associated training definition that are accessible to participant by user ref id.
     *
     * @param trainingDefinitionId the training definition id
     * @param userRefId            the participant user ref id
     * @param pageable             the pageable
     * @return the page of all {@link TrainingRun}s by id of associated {@link cz.cyberrange.platform.training.persistence.model.TrainingDefinition} that are accessible to participant
     */
    Page<TrainingRun> findAllByTrainingDefinitionIdAndParticipantUserRefId(@Param("trainingDefinitionId") Long trainingDefinitionId,
                                                                           @Param("userRefId") Long userRefId,
                                                                           Pageable pageable);

    /**
     * Find all active training runs by training instance id.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all active {@link TrainingRun}s associated with given {@link TrainingInstance}
     */
    Page<TrainingRun> findAllActiveByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId,
                                                        Pageable pageable);

    /**
     * Find all inactive training runs by training instance id.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all inactive {@link TrainingRun}s associated with given {@link TrainingInstance}
     */
    Page<TrainingRun> findAllInactiveByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId,
                                                          Pageable pageable);

    /**
     * Find all finished training runs by training instance id.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all finished {@link TrainingRun}s associated with given {@link TrainingInstance}
     */
    Page<TrainingRun> findAllFinishedByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId,
                                                          Pageable pageable);

    /**
     * Find all training runs associated with training definition.
     *
     * @param trainingDefinitionId the training definition id
     * @param pageable             the pageable
     * @return the page of all {@link TrainingRun}s associated with {@link cz.cyberrange.platform.training.persistence.model.TrainingDefinition}
     */
    Page<TrainingRun> findAllByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId,
                                                    Pageable pageable);

    /**
     * Delete all training runs by training instance.
     *
     * @param trainingInstanceId the training instance id
     */
    @Modifying
    void deleteTrainingRunsByTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

    /**
     * Find valid training run by user and access token (must have sandbox assigned).
     *
     * @param accessToken the access token
     * @param userRefId   the user ref id
     * @return the {@link TrainingRun} by user and access token
     */
    Optional<TrainingRun> findRunningTrainingRunOfUser(@Param("accessToken") String accessToken, @Param("userRefId") Long userRefId);

    /**
     * Find running training run by user and access token, with or without sandbox.
     * Used when entering run: after detaching removed sandbox we still need to find this run to return it with allowAllocate.
     *
     * @param accessToken the access token
     * @param userRefId   the user ref id
     * @return the {@link TrainingRun} if any
     */
    Optional<TrainingRun> findRunningTrainingRunOfUserWithOrWithoutSandbox(@Param("accessToken") String accessToken, @Param("userRefId") Long userRefId);

    /**
     * Exists any for training instance boolean.
     *
     * @param trainingInstanceId the training instance id
     * @return the boolean
     */
    boolean existsAnyForTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

    /**
     * Find training runs that reference the given sandbox (for detaching when trainee requests cleanup).
     *
     * @param sandboxInstanceRefId the sandbox UUID
     * @return list of training runs with this sandbox
     */
    List<TrainingRun> findBySandboxInstanceRefId(@Param("sandboxInstanceRefId") String sandboxInstanceRefId);

    /**
     * True if the given user has at least one running (not finished/archived) training run that uses this sandbox.
     * Used by sandbox-service to verify a trainee may access a specific sandbox (e.g. topology) when pool is locked.
     *
     * @param userRefId            participant user ref id (from JWT)
     * @param sandboxInstanceRefId sandbox UUID
     * @return true if user has an active run with this sandbox
     */
    @Query("SELECT CASE WHEN COUNT(tr) > 0 THEN true ELSE false END FROM TrainingRun tr JOIN tr.participantRef pr "
            + "WHERE pr.userRefId = :userRefId AND tr.sandboxInstanceRefId = :sandboxInstanceRefId "
            + "AND tr.state NOT IN ('FINISHED', 'ARCHIVED')")
    boolean existsRunningRunByUserRefIdAndSandboxInstanceRefId(
            @Param("userRefId") Long userRefId,
            @Param("sandboxInstanceRefId") String sandboxInstanceRefId);

    /**
     * Find running training runs (not finished/archived) for the given participant that have this allocation unit id.
     * Used to verify ownership when requesting cleanup by allocation id (e.g. when sandbox_id is null).
     */
    @Query("SELECT tr FROM TrainingRun tr JOIN FETCH tr.trainingInstance JOIN tr.participantRef pr "
            + "WHERE pr.userRefId = :userRefId AND tr.sandboxInstanceAllocationId = :allocationUnitId "
            + "AND tr.state NOT IN ('FINISHED', 'ARCHIVED')")
    List<TrainingRun> findRunningByParticipantRefIdAndAllocationId(
            @Param("userRefId") Long userRefId,
            @Param("allocationUnitId") Integer allocationUnitId);

    /**
     * Find any running training run (any participant) that uses this allocation unit id.
     * Used for managed instances to determine if a pool allocation is free (not attached to any run).
     */
    @Query("SELECT tr FROM TrainingRun tr WHERE tr.sandboxInstanceAllocationId = :allocationUnitId "
            + "AND tr.state NOT IN ('FINISHED', 'ARCHIVED')")
    List<TrainingRun> findRunningByAllocationId(@Param("allocationUnitId") Integer allocationUnitId);

    /**
     * Find running training runs (not finished/archived) that have a sandbox, for the given participant.
     * Used for single-sandbox-per-user: block new allocation when user already has a run with sandbox elsewhere.
     *
     * @param userRefId the participant user ref id
     * @return list of runs with sandbox for this user
     */
    @Query("SELECT tr FROM TrainingRun tr JOIN FETCH tr.trainingInstance JOIN tr.participantRef pr "
            + "WHERE pr.userRefId = :userRefId AND tr.sandboxInstanceRefId IS NOT NULL "
            + "AND tr.state NOT IN ('FINISHED', 'ARCHIVED')")
    List<TrainingRun> findRunningWithSandboxByParticipantRefId(@Param("userRefId") Long userRefId);

    /**
     * Find running training runs (not finished/archived) that have an allocation id (sandbox building or ready).
     * Used so active-sandboxes list includes allocations that are still building (sandbox_instance_ref_id null).
     *
     * @param userRefId the participant user ref id
     * @return list of runs with allocation for this user
     */
    @Query("SELECT tr FROM TrainingRun tr JOIN FETCH tr.trainingInstance JOIN tr.participantRef pr "
            + "WHERE pr.userRefId = :userRefId AND tr.sandboxInstanceAllocationId IS NOT NULL "
            + "AND tr.state NOT IN ('FINISHED', 'ARCHIVED')")
    List<TrainingRun> findRunningWithAllocationByParticipantRefId(@Param("userRefId") Long userRefId);

}
