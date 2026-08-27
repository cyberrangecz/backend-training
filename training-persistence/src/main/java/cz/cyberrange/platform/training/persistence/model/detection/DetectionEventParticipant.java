package cz.cyberrange.platform.training.persistence.model.detection;

import cz.cyberrange.platform.training.persistence.model.AbstractEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * One trainee implicated in one finding, kept alongside the details of the submission that put them
 * there. A trainee implicated in several findings has a row for each.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "detection_event_participant")
@NamedQueries({
  @NamedQuery(
      name = "DetectionEventParticipant.findAllByEventId",
      query =
          "SELECT dep FROM DetectionEventParticipant dep "
              + "WHERE dep.detectionEventId = :eventId "
              + "ORDER BY dep.occurredAt"),
  @NamedQuery(
      name = "DetectionEventParticipant.deleteAllParticipantsByCheatingDetectionId",
      query =
          "DELETE FROM DetectionEventParticipant dep WHERE dep.cheatingDetectionId = :cheatingDetectionId")
})
public class DetectionEventParticipant extends AbstractEntity<Long> {

  @Column(name = "ip_address", nullable = true)
  private String ipAddress;

  @Column(name = "occurred_at", nullable = true)
  private LocalDateTime occurredAt;

  @Column(name = "participant_name", nullable = false)
  private String participantName;

  /**
   * How long the trainee took over the level, in seconds. Set only where the finding turns on
   * solving speed.
   */
  @Column(name = "solved_in_time", nullable = true)
  private Long solvedInTime;

  /**
   * The trainee's {@code userRefId}, the id spoken outside this service, rather than the primary
   * key of their {@code user_ref} row.
   */
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "detection_event_id", nullable = false)
  private Long detectionEventId;

  @Column(name = "cheating_detection_id", nullable = false)
  private Long cheatingDetectionId;
}
