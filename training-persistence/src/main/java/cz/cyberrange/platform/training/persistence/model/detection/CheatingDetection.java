package cz.cyberrange.platform.training.persistence.model.detection;

import cz.cyberrange.platform.training.persistence.model.AbstractEntity;
import cz.cyberrange.platform.training.persistence.model.enums.CheatingDetectionState;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * One sweep for cheating over a single training instance. It holds who asked for the sweep and
 * when, the settings the individual detections need, and a state per detection so that each can be
 * followed, skipped or seen to have finished on its own.
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@ToString
@Entity
@Table(name = "cheating_detection")
@NamedQueries({
  @NamedQuery(
      name = "CheatingDetection.findAllByTrainingInstanceId",
      query =
          "SELECT cd FROM CheatingDetection cd "
              + "WHERE cd.trainingInstanceId = :trainingInstanceId "
              + "ORDER BY cd.executeTime"),
  @NamedQuery(
      name = "CheatingDetection.findCheatingDetectionById",
      query = "SELECT cd FROM CheatingDetection cd " + "WHERE cd.id = :cheatingDetectionId"),
  @NamedQuery(
      name = "CheatingDetection.deleteCheatingDetectionById",
      query = "DELETE FROM CheatingDetection cd WHERE cd.id = :cheatingDetectionId"),
  @NamedQuery(
      name = "CheatingDetection.deleteAllCheatingDetectionsOfTrainingInstance",
      query = "DELETE FROM CheatingDetection cd WHERE cd.trainingInstanceId = :trainingInstanceId")
})
public class CheatingDetection extends AbstractEntity<Long> {

  @Column(name = "training_instance_id", nullable = false)
  private Long trainingInstanceId;

  /** Display name of the user who asked for the sweep, kept as text rather than as a reference. */
  @Column(name = "executed_by")
  private String executedBy;

  @Column(name = "execute_time", nullable = false)
  private LocalDateTime executeTime;

  @Column(name = "proximity_threshold", nullable = true)
  private Long proximityThreshold;

  @Enumerated(EnumType.STRING)
  @Column(name = "current_state", nullable = false)
  private CheatingDetectionState currentState;

  /** How many findings the sweep has made, recounted as the detections report. */
  @Column(name = "results")
  private Long results;

  @Enumerated(EnumType.STRING)
  @Column(name = "answer_similarity_state")
  private CheatingDetectionState answerSimilarityState;

  @Enumerated(EnumType.STRING)
  @Column(name = "location_similarity_state")
  private CheatingDetectionState locationSimilarityState;

  @Enumerated(EnumType.STRING)
  @Column(name = "time_proximity_state")
  private CheatingDetectionState timeProximityState;

  @Enumerated(EnumType.STRING)
  @Column(name = "minimal_solve_time_state")
  private CheatingDetectionState minimalSolveTimeState;

  @Enumerated(EnumType.STRING)
  @Column(name = "forbidden_commands_state")
  private CheatingDetectionState forbiddenCommandsState;

  @Enumerated(EnumType.STRING)
  @Column(name = "no_commands_state")
  private CheatingDetectionState noCommandsState;

  @OneToMany(
      mappedBy = "cheatingDetection",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<ForbiddenCommand> commands = new ArrayList<>();

  /**
   * Queues a detection unless it was asked to be left out, in which case it stays left out.
   *
   * @param state the state the detection currently carries
   * @return the state it should carry once the sweep starts
   */
  private CheatingDetectionState setExecuteState(CheatingDetectionState state) {
    return state != CheatingDetectionState.DISABLED
        ? CheatingDetectionState.QUEUED
        : CheatingDetectionState.DISABLED;
  }

  /**
   * Marks the sweep as running and queues each of the six detections that was not left out, so that
   * every detection is either waiting to run or explicitly excluded.
   */
  public void setExecuteStates() {
    this.setCurrentState(CheatingDetectionState.RUNNING);
    this.setAnswerSimilarityState(setExecuteState(this.getAnswerSimilarityState()));
    this.setLocationSimilarityState(setExecuteState(this.getLocationSimilarityState()));
    this.setMinimalSolveTimeState(setExecuteState(this.getMinimalSolveTimeState()));
    this.setTimeProximityState(setExecuteState(this.getTimeProximityState()));
    this.setNoCommandsState(setExecuteState(this.getNoCommandsState()));
    this.setForbiddenCommandsState(setExecuteState(this.getForbiddenCommandsState()));
  }
}
