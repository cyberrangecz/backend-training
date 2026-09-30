package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.training.persistence.model.QTrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link TrainingInstance} instances */
@Repository
public interface TrainingInstanceRepository
    extends JpaRepository<TrainingInstance, Long>,
        TrainingInstanceRepositoryCustom,
        QuerydslPredicateExecutor<TrainingInstance>,
        QuerydslBinderCustomizer<QTrainingInstance> {

  /**
   * That method is used to make the query dsl string values case insensitive and also it supports
   * partial matches in the database.
   *
   * @param querydslBindings
   * @param qTrainingInstance
   */
  @Override
  default void customize(QuerydslBindings querydslBindings, QTrainingInstance qTrainingInstance) {
    querydslBindings
        .bind(String.class)
        .all(
            (StringPath path, Collection<? extends String> values) -> {
              BooleanBuilder predicate = new BooleanBuilder();
              values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
              return Optional.ofNullable(predicate);
            });
  }

  /**
   * Finds the training instances belonging to the given training definition. Established by the
   * {@code TrainingInstance.findAllByTrainingDefinitionId} named query declared on {@link
   * TrainingInstance}, which loads each instance's training definition eagerly; the id matched is
   * the definition's own primary key. Returns an empty list when the definition has no instances.
   *
   * @param trainingDefId the training def id
   * @return the list of {@link TrainingInstance}s associated to {@link TrainingDefinition}
   */
  List<TrainingInstance> findAllByTrainingDefinitionId(@Param("trainingDefId") Long trainingDefId);

  /**
   * Finds, among the given training definitions, the ids of those with at least one training
   * instance whose end time is after the given moment. Established by the {@code
   * TrainingInstance.findTrainingDefinitionIdsWithInstanceEndingAfter} named query declared on
   * {@link TrainingInstance}. Returns an empty list when none match.
   *
   * @param trainingDefinitionIds the training definition ids to restrict the lookup to
   * @param time the moment an instance has to end after
   * @return the ids of the matching {@link TrainingDefinition}s
   */
  List<Long> findTrainingDefinitionIdsWithInstanceEndingAfter(
      @Param("trainingDefinitionIds") Collection<Long> trainingDefinitionIds,
      @Param("time") LocalDateTime time);

  /**
   * Finds every training instance matching the given predicate. The entity graph loads each
   * instance's organizers, its training definition, that definition's authors, that definition's
   * beta testing group, and that group's organizers eagerly.
   *
   * @param predicate the predicate
   * @param pageable the pageable
   * @return page of all {@link TrainingInstance}
   */
  @EntityGraph(
      value = "TrainingInstance.findAllAuthorsOrganizersBetaTestingGroupBetaTestingGroupOrganizers",
      type = EntityGraph.EntityGraphType.FETCH)
  Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable);

  /**
   * Finds the training instance with the given primary key. The entity graph loads its organizers,
   * its training definition, and that definition's authors eagerly.
   *
   * @param id id of training instance
   * @return {@link TrainingInstance}, empty when no instance has that id
   */
  @EntityGraph(
      value = "TrainingInstance.findByIdAuthorsOrganizers",
      type = EntityGraph.EntityGraphType.FETCH)
  Optional<TrainingInstance> findById(Long id);

  /**
   * Finds the training instances whose primary key is one of the given ids. The entity graph loads
   * each instance's organizers, its training definition, and that definition's authors eagerly.
   *
   * @param ids the ids of training instances to find
   * @return the list of {@link TrainingInstance}s with organizers eagerly loaded
   */
  @EntityGraph(
      value = "TrainingInstance.findByIdAuthorsOrganizers",
      type = EntityGraph.EntityGraphType.FETCH)
  List<TrainingInstance> findAllByIdIn(@Param("ids") List<Long> ids);

  /**
   * Finds the training instance with the given access token whose window currently contains the
   * given moment. Established by the {@code
   * TrainingInstance.findByStartTimeAfterAndEndTimeBeforeAndAccessToken} named query declared on
   * {@link TrainingInstance}, whose actual condition is {@code startTime < datetime AND endTime >
   * datetime} — the given moment falls between the instance's start and end time. This is the
   * opposite of what the method name would derive on its own; the named query, not the name,
   * decides the comparison direction. Loads the matched instance's training definition eagerly.
   *
   * @param datetime the moment that must fall within the instance's start and end time
   * @param accessToken the access token
   * @return the matching {@link TrainingInstance}, empty when none matches
   */
  Optional<TrainingInstance> findByStartTimeAfterAndEndTimeBeforeAndAccessToken(
      @Param("datetime") LocalDateTime datetime, @Param("accessToken") String accessToken);

  /**
   * Checks whether any training instance is associated with the given training definition.
   * Established by the {@code TrainingInstance.existsAnyForTrainingDefinition} named query declared
   * on {@link TrainingInstance}; the id matched is the definition's own primary key.
   *
   * @param trainingDefinitionId the training definition id
   * @return True if there are any instances associated with training definition
   */
  boolean existsAnyForTrainingDefinition(@Param("trainingDefinitionId") Long trainingDefinitionId);

  /**
   * Checks whether a training instance exists with the given access token. The {@code @Query} on
   * this method is the source of the query text.
   *
   * @param accessToken the access token
   * @return True if there is any instance with given access token
   */
  @Query(
      "SELECT COUNT(*) > 0 FROM TrainingInstance instance WHERE instance.accessToken = :accessToken")
  boolean existsForToken(@Param("accessToken") String accessToken);

  /**
   * Finds the training instance with the given primary key together with its associated data.
   * Established by the {@code TrainingInstance.findByIdIncludingDefinition} named query declared on
   * {@link TrainingInstance}, which loads the instance's organizers, its training definition, that
   * definition's authors, that definition's beta testing group, and that group's organizers
   * eagerly.
   *
   * @param instanceId the instance id
   * @return {@link TrainingInstance} including its associated {@link TrainingDefinition}
   */
  Optional<TrainingInstance> findByIdIncludingDefinition(@Param("instanceId") Long instanceId);

  /**
   * Finds the training instance assigned the given sandbox pool. Established by the {@code
   * TrainingInstance.findByPoolId} named query declared on {@link TrainingInstance}, which fetches
   * no association eagerly.
   *
   * @param poolId the pool id
   * @return the {@link TrainingInstance} assigned that pool, empty when no instance is
   */
  Optional<TrainingInstance> findByPoolId(@Param("poolId") Long poolId);

  /**
   * Checks whether the given training instance's end time is before the given moment. Established
   * by the {@code TrainingInstance.isFinished} named query declared on {@link TrainingInstance}.
   *
   * @param instanceId the instance id
   * @param currentTime the current time
   * @return true if instance is finished, false if not
   */
  boolean isFinished(
      @Param("instanceId") Long instanceId, @Param("currentTime") LocalDateTime currentTime);

  /**
   * Finds the training instances whose organizers include the given user. Derived from the method
   * name, with no named query of this name on {@link TrainingInstance}; the {@code organizers} join
   * table's {@code user_ref_id} column carries no explicit {@code referencedColumnName}, so JPA
   * defaults it to the referenced entity's primary key. Membership is therefore decided by {@link
   * UserRef#getId()}, not {@link UserRef#getUserRefId()}, despite the column name; the given {@code
   * organizer} must already carry the correct primary key.
   *
   * @param organizer the user whose organized training instances are to be found
   * @return the list of {@link TrainingInstance}s whose organizers contain the given user
   */
  List<TrainingInstance> findAllByOrganizersContains(UserRef organizer);
}
