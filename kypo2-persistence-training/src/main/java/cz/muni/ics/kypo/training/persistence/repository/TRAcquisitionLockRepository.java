package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.TRAcquisitionLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * The JPA repository interface to manage {@link TRAcquisitionLock} instances.
 *
 */
@Repository
public interface TRAcquisitionLockRepository extends JpaRepository<TRAcquisitionLock, Long>, QuerydslPredicateExecutor<TRAcquisitionLockRepository> {

    /**
     * Deletes Acquisition Lock by participant and training instance
     * @param participantId - id of participant associated with lock
     * @param trainingInstanceId - id of training instance associated with lock
     */
    void deleteByParticipantRefIdAndTrainingInstanceId(@Param("participantId") Long participantId,@Param("trainingInstanceId") Long trainingInstanceId);
}
