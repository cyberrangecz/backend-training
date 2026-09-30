package cz.cyberrange.platform.training.persistence.model;

import cz.cyberrange.platform.training.persistence.model.enums.SubmissionType;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * One recorded attempt at answering a training level of a run. Nothing reads these rows back for
 * scoring, which is written onto the run itself; they exist as the evidence the cheating detections
 * in {@code services/detection} work from.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "submission")
@NamedQueries({
  @NamedQuery(
      name = "Submission.getCorrectSubmissionsOfTrainingRunSorted",
      query =
          "SELECT s FROM Submission s "
              + "JOIN FETCH s.trainingRun tr "
              + "WHERE s.type ='CORRECT' AND tr.id = :trainingRunId "
              + "ORDER BY s.date"),
  @NamedQuery(
      name = "Submission.getCorrectSubmissionsOfTrainingInstance",
      query =
          "SELECT s FROM Submission s "
              + "JOIN FETCH s.trainingRun tr "
              + "JOIN FETCH tr.trainingInstance ti "
              + "WHERE s.type = 'CORRECT' AND ti.id = :trainingInstanceId "
              + "ORDER BY tr.id, s.date"),
  // Orders by pr.userRefId, the external user-and-group identifier, not the local UserRef
  // primary key.
  @NamedQuery(
      name = "Submission.getIncorrectSubmissionsOfTrainingInstance",
      query =
          "SELECT s FROM Submission s "
              + "JOIN FETCH s.trainingRun tr "
              + "JOIN FETCH tr.trainingInstance ti "
              + "JOIN FETCH tr.participantRef pr "
              + "WHERE s.type = 'INCORRECT' AND ti.id = :trainingInstanceId "
              + "ORDER BY pr.userRefId, s.date"),
  @NamedQuery(
      name = "Submission.getAllTimeProximitySubmissionsOfLevel",
      query =
          "SELECT s FROM Submission s "
              + "JOIN FETCH s.trainingRun tr "
              + "JOIN FETCH tr.trainingInstance ti "
              + "JOIN FETCH s.level l "
              + "WHERE s.type = 'CORRECT' AND ti.id = :trainingInstanceId AND l.id = :levelId "
              + "ORDER BY s.date"),
  @NamedQuery(
      name = "Submission.getSubmissionsByLevelAndInstance",
      query =
          "SELECT s FROM Submission s "
              + "JOIN FETCH s.trainingRun tr "
              + "JOIN FETCH tr.trainingInstance ti "
              + "JOIN FETCH s.level l "
              + "WHERE l.id = :levelId AND ti.id = :trainingInstanceId "
              + "ORDER BY tr.id")
})
public class Submission extends AbstractEntity<Long> implements Serializable {

  /**
   * The answer text the trainee submitted to a training level, as they typed it. A passkey attempt
   * produces no row here.
   */
  @Column(name = "provided", nullable = false)
  private String provided;

  // Whether the submission matched the expected answer.
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private SubmissionType type;

  // Server time at which the submission was recorded.
  @Column(name = "date", nullable = false)
  private LocalDateTime date;

  // Value of the submitting request's x-real-ip header, or an empty string when the header is
  // absent.
  @Column(name = "ip_address", nullable = false)
  private String ipAddress;

  // The level being attempted at the time of submission.
  @JoinColumn(name = "level_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private AbstractLevel level;

  // The training run the submission was made in.
  @JoinColumn(name = "training_run_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private TrainingRun trainingRun;
}
