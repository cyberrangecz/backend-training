package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.UserRef;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link UserRef} instances. */
@Repository
public interface UserRefRepository
    extends JpaRepository<UserRef, Long>,
        QuerydslPredicateExecutor<UserRef>,
        UserRefRepositoryCustom {

  /**
   * Finds the users whose cross-service {@code user_ref_id} column is one of the given values.
   * Established by the {@code UserRef.findUsers} named query declared on {@link UserRef}; matches
   * {@link UserRef#getUserRefId()}, not the local primary key. Returns an empty set when none
   * match.
   *
   * @param userRefId the user ref ids
   * @return the set of {@link UserRef}
   */
  Set<UserRef> findUsers(@Param("userRefId") Set<Long> userRefId);

  /**
   * Finds the user whose cross-service {@code user_ref_id} column equals the given value.
   * Established by the {@code UserRef.findUserByUserRefId} named query declared on {@link UserRef};
   * matches {@link UserRef#getUserRefId()}, not the local primary key.
   *
   * @param userRefId the user ref id
   * @return the {@link UserRef}, empty when none matches
   */
  Optional<UserRef> findUserByUserRefId(@Param("userRefId") Long userRefId);

  /**
   * Finds the cross-service {@code userRefId} of every participant of the given training instance.
   * Established by the {@code UserRef.findParticipantsRefIdsByTrainingInstanceId} named query
   * declared on {@link UserRef}, which reaches the participants through each {@code TrainingRun} of
   * the instance and returns their {@link UserRef#getUserRefId()} values, not local primary keys.
   * The instance id itself is matched against its own single primary key. Returns an empty set when
   * the instance has no training runs.
   *
   * @param trainingInstanceId id of the training instance
   * @return the cross-service ids of the participants
   */
  Set<Long> findParticipantsRefIdsByTrainingInstanceId(
      @Param("trainingInstanceId") Long trainingInstanceId);
}
