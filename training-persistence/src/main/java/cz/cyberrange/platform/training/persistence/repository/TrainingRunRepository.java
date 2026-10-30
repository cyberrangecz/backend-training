package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.training.persistence.model.QTrainingRun;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

/** The JPA repository interface to manage {@link TrainingRun} instances */
@Repository
public interface TrainingRunRepository
    extends JpaRepository<TrainingRun, Long>,
        TrainingRunRepositoryCustom,
        QuerydslPredicateExecutor<TrainingRun>,
        QuerydslBinderCustomizer<QTrainingRun> {

  String FIND_ALL_BY_PARTICIPANT_USER_REF_ID =
      "SELECT tr FROM TrainingRun tr "
          + "INNER JOIN tr.participantRef pr "
          + "INNER JOIN tr.trainingInstance ti "
          + "INNER JOIN ti.trainingDefinition "
          + "WHERE pr.userRefId = :userRefId";

  /**
   * That method is used to make the query dsl string values case and accent insensitive and also
   * it supports partial matches in the database.
   *
   * @param querydslBindings
   * @param qTrainingRun
   */
  @Override
  default void customize(QuerydslBindings querydslBindings, QTrainingRun qTrainingRun) {
    querydslBindings
        .bind(String.class)
        .all(
            (StringPath path, Collection<? extends String> values) -> {
              BooleanBuilder predicate = new BooleanBuilder();
              values.forEach(value -> predicate.and(FoldedSearchPredicate.contains(path, value)));
              return Optional.ofNullable(predicate);
            });
  }

  /**
   * Finds all training runs of a training instance, ordered by the page's sort. The backing query
   * matches on {@code trainingInstance.id}; the {@code @EntityGraph} fetches each run's participant
   * reference eagerly.
   *
   * @param trainingInstanceId the primary key of the training instance
   * @param pageable the page to return and its sort
   * @return the page of {@link TrainingRun}s of the {@link TrainingInstance}, or an empty page if
   *     none exist
   */
  @Query("SELECT tr FROM TrainingRun tr WHERE tr.trainingInstance.id = :trainingInstanceId")
  @EntityGraph(
      value = "TrainingRun.findAllParticipantRef",
      type = EntityGraph.EntityGraphType.FETCH)
  Page<TrainingRun> findAllByTrainingInstanceId(
      @Param("trainingInstanceId") Long trainingInstanceId, Pageable pageable);

  /**
   * Finds all training runs of a training instance, matched on {@code trainingInstance.id} and
   * ordered by id. The {@code @EntityGraph} fetches each run's participant reference eagerly.
   *
   * @param trainingInstanceId the primary key of the training instance
   * @return the set of {@link TrainingRun}s of the {@link TrainingInstance}, or an empty set if
   *     none exist
   */
  @EntityGraph(
      value = "TrainingRun.findAllParticipantRef",
      type = EntityGraph.EntityGraphType.FETCH)
  Set<TrainingRun> findAllByTrainingInstanceIdOrderById(Long trainingInstanceId);

  /**
   * Find training run by id.
   *
   * @param id id of training run
   * @return {@link TrainingRun}
   */
  @EntityGraph(
      value = "TrainingRun.findByIdParticipantRefTrainingInstance",
      type = EntityGraph.EntityGraphType.FETCH)
  Optional<TrainingRun> findById(Long id);

  /**
   * Find all training runs accessed by the participant holding the given user-and-group id. Matches
   * on {@code participantRef.userRefId}, not on the participant row's primary key.
   *
   * @param userRefId the user-and-group id of the participant
   * @param pageable the page to return and its sort
   * @return the page of all {@link TrainingRun}s accessed by participant
   */
  @Query(FIND_ALL_BY_PARTICIPANT_USER_REF_ID)
  Page<TrainingRun> findAllByParticipantRefId(
      @Param("userRefId") Long userRefId, Pageable pageable);

  /**
   * Find all training runs accessed by the participant holding the given user-and-group id. Matches
   * on {@code participantRef.userRefId}, not on the participant row's primary key.
   *
   * @param userRefId the user-and-group id of the participant
   * @return the list of all {@link TrainingRun}s accessed by participant
   */
  @Query(FIND_ALL_BY_PARTICIPANT_USER_REF_ID + " ORDER BY tr.id")
  List<TrainingRun> findAllByParticipantRefId(@Param("userRefId") Long userRefId);

  /**
   * Find training run by id including current level.
   *
   * @param trainingRunId the training run id
   * @return {@link TrainingRun} including {@link
   *     cz.cyberrange.platform.training.persistence.model.AbstractLevel}
   */
  Optional<TrainingRun> findByIdWithLevel(@Param("trainingRunId") Long trainingRunId);

  /**
   * Find all training runs by id of associated training definition that are accessible to
   * participant by user ref id.
   *
   * @param trainingDefinitionId the training definition id
   * @param userRefId the participant user ref id
   * @param pageable the pageable
   * @return the page of all {@link TrainingRun}s by id of associated {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition} that are accessible
   *     to participant
   */
  @Query(
      "SELECT tr FROM TrainingRun tr "
          + "INNER JOIN tr.participantRef pr "
          + "INNER JOIN tr.trainingInstance ti "
          + "INNER JOIN ti.trainingDefinition td "
          + "WHERE td.id = :trainingDefinitionId AND pr.userRefId = :userRefId")
  Page<TrainingRun> findAllByTrainingDefinitionIdAndParticipantUserRefId(
      @Param("trainingDefinitionId") Long trainingDefinitionId,
      @Param("userRefId") Long userRefId,
      Pageable pageable);

  /**
   * Find all active training runs by training instance id.
   *
   * @param trainingInstanceId the training instance id
   * @param pageable the pageable
   * @return the page of all active {@link TrainingRun}s associated with given {@link
   *     TrainingInstance}
   */
  @Query(
      "SELECT tr FROM TrainingRun tr INNER JOIN tr.trainingInstance ti "
          + "WHERE ti.id = :trainingInstanceId AND tr.state <> 'ARCHIVED'")
  Page<TrainingRun> findAllActiveByTrainingInstanceId(
      @Param("trainingInstanceId") Long trainingInstanceId, Pageable pageable);

  /**
   * Find all inactive training runs by training instance id.
   *
   * @param trainingInstanceId the training instance id
   * @param pageable the pageable
   * @return the page of all inactive {@link TrainingRun}s associated with given {@link
   *     TrainingInstance}
   */
  @Query(
      "SELECT tr FROM TrainingRun tr INNER JOIN tr.trainingInstance ti "
          + "WHERE ti.id = :trainingInstanceId AND tr.state = 'ARCHIVED'")
  Page<TrainingRun> findAllInactiveByTrainingInstanceId(
      @Param("trainingInstanceId") Long trainingInstanceId, Pageable pageable);

  /**
   * Find all finished training runs by training instance id.
   *
   * @param trainingInstanceId the training instance id
   * @param pageable the pageable
   * @return the page of all finished {@link TrainingRun}s associated with given {@link
   *     TrainingInstance}
   */
  @Query(
      "SELECT tr FROM TrainingRun tr INNER JOIN tr.trainingInstance ti "
          + "WHERE ti.id = :trainingInstanceId AND tr.state = 'FINISHED'")
  Page<TrainingRun> findAllFinishedByTrainingInstanceId(
      @Param("trainingInstanceId") Long trainingInstanceId, Pageable pageable);

  /**
   * Find all training runs associated with training definition.
   *
   * @param trainingDefinitionId the training definition id
   * @param pageable the pageable
   * @return the page of all {@link TrainingRun}s associated with {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition}
   */
  @Query(
      "SELECT tr FROM TrainingRun tr "
          + "INNER JOIN tr.trainingInstance ti "
          + "INNER JOIN ti.trainingDefinition td "
          + "WHERE td.id = :trainingDefinitionId")
  Page<TrainingRun> findAllByTrainingDefinitionId(
      @Param("trainingDefinitionId") Long trainingDefinitionId, Pageable pageable);

  /**
   * Delete all training runs by training instance.
   *
   * @param trainingInstanceId the training instance id
   */
  @Modifying
  void deleteTrainingRunsByTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

  /**
   * Find valid training run by user and access token.
   *
   * @param accessToken the access token
   * @param userRefId the user ref id
   * @return the {@link TrainingRun} by user and access token
   */
  Optional<TrainingRun> findRunningTrainingRunOfUser(
      @Param("accessToken") String accessToken, @Param("userRefId") Long userRefId);

  /**
   * Finds the running training run of the given user for the instance with the given access token,
   * whether or not a sandbox is assigned to it yet.
   *
   * @param accessToken the access token
   * @param userRefId the cross-service user reference id of the participant
   * @return the {@link TrainingRun} by user and access token
   */
  Optional<TrainingRun> findUnfinishedTrainingRunOfUser(
      @Param("accessToken") String accessToken, @Param("userRefId") Long userRefId);

  /**
   * Finds the training runs that the given sandbox allocation units were allocated for, with their
   * training instances.
   *
   * @param allocationIds the ids of the allocation units in the sandbox service
   * @return the training runs, in no particular order
   */
  @Query(
      "SELECT tr FROM TrainingRun tr JOIN FETCH tr.trainingInstance "
          + "WHERE tr.sandboxInstanceAllocationId IN :allocationIds")
  List<TrainingRun> findAllBySandboxInstanceAllocationIds(
      @Param("allocationIds") Collection<Integer> allocationIds);

  /**
   * Checks whether a training run works, or worked before its sandbox was replaced or the run was
   * archived, in the given sandbox.
   *
   * @param sandboxInstanceRefId the sandbox UUID to look for as the current sandbox
   * @param previousSandboxInstanceRefId the sandbox UUID to look for as the previous sandbox
   * @return true when such a run exists
   */
  boolean existsBySandboxInstanceRefIdOrPreviousSandboxInstanceRefId(
      String sandboxInstanceRefId, String previousSandboxInstanceRefId);

  /**
   * Exists any for training instance boolean.
   *
   * @param trainingInstanceId the training instance id
   * @return the boolean
   */
  boolean existsAnyForTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

  /**
   * Find all training runs whose participant row has one of the given primary keys. Matches on
   * {@code participantRef.id} — the training service's own key, not the user-and-group id that
   * {@link #findAllByParticipantRefId(Long)} takes despite the matching name. A caller holding
   * user-and-group ids resolves them to participant rows first.
   *
   * @param participantRefIds the primary keys of the participant rows
   * @return the list of all {@link TrainingRun}s accessed by those participants
   */
  List<TrainingRun> findAllByParticipantRefIdInOrderById(List<Long> participantRefIds);
}
