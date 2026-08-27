package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

/** Manages persistence of {@link CheatingDetection} sweep records. */
public interface CheatingDetectionRepository
    extends JpaRepository<CheatingDetection, Long>, QuerydslPredicateExecutor<CheatingDetection> {

  /**
   * Returns, as one page, the cheating detection sweeps of one training instance, ordered by their
   * execute time.
   *
   * @param trainingInstanceId the training instance the returned sweeps belong to
   * @param pageable the page to return
   */
  Page<CheatingDetection> findAllByTrainingInstanceId(
      @Param("trainingInstanceId") Long trainingInstanceId, Pageable pageable);

  /**
   * Returns every cheating detection sweep of one training instance, ordered by their execute time.
   *
   * @param trainingInstanceId the training instance the returned sweeps belong to
   */
  List<CheatingDetection> findAllByTrainingInstanceId(
      @Param("trainingInstanceId") Long trainingInstanceId);

  /**
   * Returns the cheating detection sweep with the given primary key.
   *
   * @param cheatingDetectionId the primary key of the sweep
   */
  CheatingDetection findCheatingDetectionById(
      @Param("cheatingDetectionId") Long cheatingDetectionId);

  /**
   * Deletes the cheating detection sweep with the given primary key in a single bulk statement,
   * which bypasses the persistence context. The sweep's {@code commands} association is configured
   * with cascading removal and orphan removal, but neither fires for a bulk delete, so any {@code
   * ForbiddenCommand} row still referencing this sweep is left behind.
   *
   * @param cheatingDetectionId the primary key of the sweep to delete
   */
  @Modifying
  void deleteCheatingDetectionById(@Param("cheatingDetectionId") Long cheatingDetectionId);

  /**
   * Deletes every cheating detection sweep of one training instance in a single bulk statement,
   * which bypasses the persistence context. As with {@link #deleteCheatingDetectionById}, this does
   * not cascade to the {@code commands} association, so any {@code ForbiddenCommand} row
   * referencing one of the deleted sweeps is left behind.
   *
   * @param trainingInstanceId the training instance whose sweeps are deleted
   */
  @Modifying
  void deleteAllCheatingDetectionsOfTrainingInstance(
      @Param("trainingInstanceId") Long trainingInstanceId);
}
