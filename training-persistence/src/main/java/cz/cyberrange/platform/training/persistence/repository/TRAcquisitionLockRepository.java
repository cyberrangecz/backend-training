package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.TRAcquisitionLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link TRAcquisitionLock} instances. */
@Repository
public interface TRAcquisitionLockRepository
    extends JpaRepository<TRAcquisitionLock, Long>,
        QuerydslPredicateExecutor<TRAcquisitionLockRepository> {

  /**
   * Deletes the lock row matching the given participant and training instance. Established by the
   * {@code TRAcquisitionLock.deleteByParticipantRefIdAndTrainingInstanceId} named query declared on
   * {@link TRAcquisitionLock}; matches the entity's own {@code participant_ref_id} column, which
   * already holds the participant's cross-service identifier rather than a local primary key. Runs
   * as a bulk delete against the database, bypassing the persistence context; {@link
   * TRAcquisitionLock} owns no further associations, so nothing is left uncascaded.
   *
   * @param participantRefId the participant's cross-service identifier
   * @param trainingInstanceId the training instance id
   */
  @Modifying
  void deleteByParticipantRefIdAndTrainingInstanceId(
      @Param("participantRefId") Long participantRefId,
      @Param("trainingInstanceId") Long trainingInstanceId);
}
