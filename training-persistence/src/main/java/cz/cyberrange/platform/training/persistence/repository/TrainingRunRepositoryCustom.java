package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

/** The interface Training instance repository custom. */
public interface TrainingRunRepositoryCustom {

  /**
   * Find all training instances of logged in user.
   *
   * @param userRefId the participant ref id
   * @param predicate represents a predicate (boolean-valued function) of one argument.
   * @param pageable the pageable
   * @return the page of training instances
   */
  Page<TrainingRun> findAllByParticipantRefId(
      @Param("userRefId") Long userRefId, Predicate predicate, Pageable pageable);

  /**
   * Find all training runs with given ids that are owned by the participant.
   *
   * @param runIds the training run ids
   * @param userRefId the participant user ref id
   * @return the list of matching training runs
   */
  List<TrainingRun> findAllByIdInAndParticipantRefId(
      @Param("runIds") List<Long> runIds, @Param("userRefId") Long userRefId);

  /**
   * Find all training runs with given ids whose training instance is organized by the user.
   *
   * @param runIds the training run ids
   * @param userRefId the organizer user ref id
   * @return the list of matching training runs
   */
  List<TrainingRun> findAllByIdInAndOrganizedByUser(
      @Param("runIds") List<Long> runIds, @Param("userRefId") Long userRefId);
}
