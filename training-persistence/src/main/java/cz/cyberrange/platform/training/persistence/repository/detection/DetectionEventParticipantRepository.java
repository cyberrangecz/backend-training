package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

/**
 * Manages {@link DetectionEventParticipant} rows, each one trainee implicated in one detection
 * event finding
 */
public interface DetectionEventParticipantRepository
    extends JpaRepository<DetectionEventParticipant, Long>,
        QuerydslPredicateExecutor<DetectionEventParticipant> {

  /**
   * Returns, as one page, the participants of one detection event, ordered by the page's sort.
   *
   * @param eventId the detection event the returned participants are implicated in
   * @param pageable the page to return and its sort
   */
  @Query("SELECT dep FROM DetectionEventParticipant dep WHERE dep.detectionEventId = :eventId")
  Page<DetectionEventParticipant> findAllByEventId(
      @Param("eventId") Long eventId, Pageable pageable);

  /**
   * Returns every participant of one detection event, ordered by the moment their submission
   * occurred, then by id.
   *
   * @param eventId the detection event the returned participants are implicated in
   */
  @Query(
      "SELECT dep FROM DetectionEventParticipant dep WHERE dep.detectionEventId = :eventId "
          + "ORDER BY dep.occurredAt, dep.id")
  List<DetectionEventParticipant> findAllByEventId(@Param("eventId") Long eventId);

  /**
   * Returns every participant of one cheating detection sweep, across every one of its detection
   * events, ordered by id.
   *
   * @param cheatingDetectionId the cheating detection the returned participants belong to
   */
  @Query(
      "SELECT dep FROM DetectionEventParticipant dep WHERE dep.cheatingDetectionId = :cheatingDetectionId ORDER BY dep.id")
  List<DetectionEventParticipant> findAllParticipantsOfCheatingDetection(
      @Param("cheatingDetectionId") Long cheatingDetectionId);

  /**
   * Deletes every participant of one cheating detection sweep in a single bulk statement, which
   * bypasses the persistence context.
   *
   * @param cheatingDetectionId the cheating detection whose participants are deleted
   */
  @Modifying
  void deleteAllParticipantsByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);

  /**
   * Finds all detection event ids by user id.
   *
   * @param userId the user id
   */
  @Query(
      "SELECT DISTINCT dep.detectionEventId FROM DetectionEventParticipant dep WHERE dep.userId = :userId")
  List<Long> getAllDetectionEventsIdsOfParticipant(@Param("userId") Long userId);
}
