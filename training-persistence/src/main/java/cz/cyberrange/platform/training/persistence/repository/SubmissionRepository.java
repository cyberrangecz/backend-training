package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link Submission} instances */
@Repository
public interface SubmissionRepository
    extends JpaRepository<Submission, Long>, QuerydslPredicateExecutor<InfoLevel> {

  /**
   * Deletes every submission whose {@code training_run_id} column equals the given {@link
   * cz.cyberrange.platform.training.persistence.model.TrainingRun} primary key. Derived from the
   * method name, with no named query of this name on {@link Submission}. Runs as a bulk delete
   * against the database, bypassing the persistence context; {@link Submission} carries no
   * cascading associations of its own, so nothing is left uncascaded.
   *
   * @param trainingRunId the training run id
   */
  @Modifying
  void deleteAllByTrainingRunId(Long trainingRunId);

  /**
   * Finds every correct submission recorded in the given training instance. Established by the
   * {@code Submission.getCorrectSubmissionsOfTrainingInstance} named query declared on {@link
   * Submission}; loads each submission's training run and that run's training instance eagerly,
   * ordered by training run id then submission date. Returns an empty list when none match.
   *
   * @param trainingInstanceId the training instance id
   * @return list of all correct {@link Submission}s of training instance.
   */
  List<Submission> getCorrectSubmissionsOfTrainingInstance(
      @Param("trainingInstanceId") Long trainingInstanceId);

  /**
   * Finds every correct submission recorded in the given training run, ordered by submission date.
   * Established by the {@code Submission.getCorrectSubmissionsOfTrainingRunSorted} named query
   * declared on {@link Submission}; loads each submission's training run eagerly. Returns an empty
   * list when none match.
   *
   * @param trainingRunId the training run id
   * @return list of all correct {@link Submission}s of training run sorted by time.
   */
  List<Submission> getCorrectSubmissionsOfTrainingRunSorted(
      @Param("trainingRunId") Long trainingRunId);

  /**
   * Finds every incorrect submission recorded in the given training instance. Established by the
   * {@code Submission.getIncorrectSubmissionsOfTrainingInstance} named query declared on {@link
   * Submission}; loads each submission's training run, that run's training instance, and that run's
   * participant reference eagerly, ordered by the participant's cross-service user identifier
   * ({@code userRefId}, not the local primary key) then submission date. Returns an empty list when
   * none match.
   *
   * @param trainingInstanceId the training instance id
   * @return list of all incorrect {@link Submission}s of training instance.
   */
  List<Submission> getIncorrectSubmissionsOfTrainingInstance(
      @Param("trainingInstanceId") Long trainingInstanceId);

  /**
   * Finds every submission made for the given level within the given training instance, regardless
   * of whether it was correct, ordered by training run id. Established by the {@code
   * Submission.getSubmissionsByLevelAndInstance} named query declared on {@link Submission}; loads
   * each submission's training run, that run's training instance, and its level eagerly. The query
   * applies no filter on IP address; grouping submissions by similar IP is left to the caller.
   * Returns an empty list when none match.
   *
   * @param trainingInstanceId the training instance id
   * @param levelId the training level
   * @return list of all {@link Submission}s of the given level and training instance.
   */
  List<Submission> getSubmissionsByLevelAndInstance(
      @Param("trainingInstanceId") Long trainingInstanceId, @Param("levelId") Long levelId);

  /**
   * Finds every correct submission made for the given level within the given training instance,
   * ordered by submission date. Established by the {@code
   * Submission.getAllTimeProximitySubmissionsOfLevel} named query declared on {@link Submission};
   * loads each submission's training run, that run's training instance, and its level eagerly.
   * Grouping the ordered results by time proximity is left to the caller; the query itself applies
   * no time-based filter. Returns an empty list when none match.
   *
   * @param trainingInstanceId the training instance id
   * @param levelId the training level
   * @return list of all correct {@link Submission}s of the given level and training instance.
   */
  List<Submission> getAllTimeProximitySubmissionsOfLevel(
      @Param("trainingInstanceId") Long trainingInstanceId, @Param("levelId") Long levelId);
}
