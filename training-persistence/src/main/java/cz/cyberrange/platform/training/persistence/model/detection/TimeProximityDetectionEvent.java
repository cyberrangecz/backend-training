package cz.cyberrange.platform.training.persistence.model.detection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A finding that several trainees solved the same level closer together in time than the sweep's
 * tolerance allows
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "time_proximity_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
  @NamedQuery(
      name = "TimeProximityDetectionEvent.findTimeProximityEventById",
      query = "SELECT tpde FROM TimeProximityDetectionEvent tpde WHERE tpde.id = :eventId"),
  @NamedQuery(
      name = "TimeProximityDetectionEvent.findAllByCheatingDetectionId",
      query =
          "SELECT tpde FROM TimeProximityDetectionEvent tpde WHERE tpde.cheatingDetectionId = :cheatingDetectionId")
})
public class TimeProximityDetectionEvent extends AbstractDetectionEvent {

  /** The tolerance the sweep ran with, copied onto the finding as it was made */
  @Column(name = "threshold")
  private Long threshold;
}
