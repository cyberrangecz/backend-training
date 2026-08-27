package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.DetectedForbiddenCommand;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

/**
 * Manages {@link DetectedForbiddenCommand} rows, each one console command recorded as matching a
 * forbidden command of a {@code ForbiddenCommandsDetectionEvent} finding.
 */
public interface DetectedForbiddenCommandRepository
    extends JpaRepository<DetectedForbiddenCommand, Long>,
        QuerydslPredicateExecutor<DetectedForbiddenCommand> {

  /**
   * Returns, as one page, the detected forbidden commands of one detection event, in no defined
   * order.
   *
   * @param eventId the detection event the returned commands were matched against
   * @param pageable the page to return
   */
  Page<DetectedForbiddenCommand> findAllByEventId(
      @Param("eventId") Long eventId, @Param("pageable") Pageable pageable);

  /**
   * Returns every detected forbidden command of one detection event, in no defined order.
   *
   * @param eventId the detection event the returned commands were matched against
   */
  List<DetectedForbiddenCommand> findAllByEventId(@Param("eventId") Long eventId);

  /**
   * Deletes every detected forbidden command of one detection event in a single bulk statement,
   * which bypasses the persistence context.
   *
   * @param eventId the detection event whose matched commands are deleted
   */
  @Modifying
  void deleteAllByDetectionEventId(@Param("eventId") Long eventId);
}
