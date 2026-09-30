package cz.cyberrange.platform.training.persistence.model.detection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A finding that a level was solved faster than it is held to be solvable, gathering everyone who
 * beat that time on it
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "minimal_solve_time_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
  @NamedQuery(
      name = "MinimalSolveTimeDetectionEvent.findMinimalSolveTimeEventById",
      query = "SELECT mstde FROM MinimalSolveTimeDetectionEvent mstde WHERE mstde.id = :eventId"),
  @NamedQuery(
      name = "MinimalSolveTimeDetectionEvent.findAllByCheatingDetectionId",
      query =
          "SELECT mstde FROM MinimalSolveTimeDetectionEvent mstde WHERE mstde.cheatingDetectionId = :cheatingDetectionId")
})
public class MinimalSolveTimeDetectionEvent extends AbstractDetectionEvent {

  /**
   * The least time the level is held to need, in seconds, converted from the minutes the level
   * itself is configured in
   */
  @Column(name = "minimal_solve_time")
  private Long minimalSolveTime;
}
