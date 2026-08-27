package cz.cyberrange.platform.training.persistence.model.detection;

import cz.cyberrange.platform.training.persistence.model.AbstractEntity;
import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * One finding a cheating detection made about a submission, holding what every kind of finding has
 * in common. Each kind of finding is a subclass with its own table joined to this one, so a finding
 * is read whole through this class and narrowed to its kind only where the extra evidence matters.
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@ToString
@Entity
@Table(name = "abstract_detection_event")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
  @NamedQuery(
      name = "AbstractDetectionEvent.deleteDetectionEventsOfTrainingInstance",
      query =
          "DELETE FROM AbstractDetectionEvent de "
              + "WHERE de.trainingInstanceId = :trainingInstanceId"),
  @NamedQuery(
      name = "AbstractDetectionEvent.findAllByCheatingDetectionId",
      query =
          "SELECT de FROM AbstractDetectionEvent de "
              + "WHERE de.cheatingDetectionId = :cheatingDetectionId"),
  @NamedQuery(
      name = "AbstractDetectionEvent.deleteDetectionEventsOfCheatingDetection",
      query =
          "DELETE FROM AbstractDetectionEvent de WHERE de.cheatingDetectionId = :cheatingDetectionId"),
  @NamedQuery(
      name = "AbstractDetectionEvent.getNumberOfDetections",
      query =
          "SELECT COUNT(de) FROM AbstractDetectionEvent de WHERE de.cheatingDetectionId = :cheatingDetectionId"),
  @NamedQuery(
      name = "AbstractDetectionEvent.findDetectionEventById",
      query = "SELECT de FROM AbstractDetectionEvent de WHERE de.id = :eventId")
})
public class AbstractDetectionEvent extends AbstractEntity<Long> {

  @Column(name = "training_instance_id", nullable = false)
  private Long trainingInstanceId;

  @Column(name = "cheating_detection_id", nullable = false)
  private Long cheatingDetectionId;

  @Column(name = "training_run_id")
  private Long trainingRunId;

  @Column(name = "level_id", nullable = false)
  private Long levelId;

  @Column(name = "level_order", nullable = false)
  private int levelOrder;

  @Column(name = "level_title", nullable = false)
  private String levelTitle;

  /**
   * The moment the detection was executed, which every finding of that detection shares, rather
   * than the moment of the submission being flagged.
   */
  @Column(name = "detected_at", nullable = false)
  private LocalDateTime detectedAt;

  /** How many trainees the finding implicates. */
  @Column(name = "participant_count", nullable = false)
  private int participantCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "detection_event_type", nullable = false)
  private DetectionEventType detectionEventType;

  /** The implicated trainees' display names, held as one string rather than as related rows. */
  @Column(name = "participants", nullable = false)
  private String participants;

  /**
   * Fills in everything a finding carries regardless of its kind, taking the instance, the moment
   * and the identity of the detection from the detection itself, and the run and the level from the
   * submission that triggered the finding. The implicated trainees' names are not set here.
   *
   * @param submission the submission the finding was made about
   * @param cd the detection that made the finding
   * @param type which kind of finding this is
   * @param size how many trainees the finding implicates
   */
  public void setCommonDetectionEventParameters(
      Submission submission, CheatingDetection cd, DetectionEventType type, int size) {
    this.setCheatingDetectionId(cd.getId());
    this.setDetectedAt(cd.getExecuteTime());
    this.setTrainingRunId(submission.getTrainingRun().getId());
    this.setLevelId(submission.getLevel().getId());
    this.setLevelOrder(submission.getLevel().getOrder());
    this.setLevelTitle(submission.getLevel().getTitle());
    this.setTrainingInstanceId(cd.getTrainingInstanceId());
    this.setDetectionEventType(type);
    this.setParticipantCount(size);
  }
}
