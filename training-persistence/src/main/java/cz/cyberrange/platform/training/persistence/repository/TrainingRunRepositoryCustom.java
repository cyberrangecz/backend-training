package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

/**
 * Custom {@link TrainingRun} lookups keyed by a participant's or an organizer's cross-service id.
 */
public interface TrainingRunRepositoryCustom {

  /**
   * Finds the training runs of the given participant, additionally narrowed by the given
   * predicate. {@code userRefId} is matched against the run's participant reference's cross-service
   * {@code userRefId}, not a local primary key.
   *
   * @param userRefId the participant's cross-service user identifier
   * @param predicate represents a predicate (boolean-valued function) of one argument.
   * @param pageable the pageable
   * @return the page of training runs
   */
  Page<TrainingRun> findAllByParticipantRefId(
      @Param("userRefId") Long userRefId, Predicate predicate, Pageable pageable);

  /**
   * Finds the training runs with one of the given ids whose participant is the given user.
   * {@code userRefId} is matched against the run's participant reference's cross-service {@code
   * userRefId}, not a local primary key.
   *
   * @param runIds the training run ids
   * @param userRefId the participant's cross-service user identifier
   * @return the list of matching training runs
   */
  List<TrainingRun> findAllByIdInAndParticipantRefId(
      @Param("runIds") List<Long> runIds, @Param("userRefId") Long userRefId);

  /**
   * Finds the training runs with one of the given ids whose training instance is organized by the
   * given user. {@code userRefId} is matched against one of the instance's organizers'
   * cross-service {@code userRefId}, not a local primary key.
   *
   * @param runIds the training run ids
   * @param userRefId the organizer's cross-service user identifier
   * @return the list of matching training runs
   */
  List<TrainingRun> findAllByIdInAndOrganizedByUser(
      @Param("runIds") List<Long> runIds, @Param("userRefId") Long userRefId);
}
