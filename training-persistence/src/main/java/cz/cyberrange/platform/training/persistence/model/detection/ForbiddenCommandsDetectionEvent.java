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
 * A finding that a trainee ran commands the sweep was told to forbid. It always implicates exactly
 * one trainee, so the inherited participant text holds a single name.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "forbidden_commands_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
  @NamedQuery(
      name = "ForbiddenCommandsDetectionEvent.findForbiddenCommandsEventById",
      query = "SELECT fcde FROM ForbiddenCommandsDetectionEvent fcde WHERE fcde.id = :eventId"),
  @NamedQuery(
      name = "ForbiddenCommandsDetectionEvent.findAllByCheatingDetectionId",
      query =
          "SELECT fcde FROM ForbiddenCommandsDetectionEvent fcde WHERE fcde.cheatingDetectionId = :cheatingDetectionId")
})
public class ForbiddenCommandsDetectionEvent extends AbstractDetectionEvent {

  /** How many matching commands the finding gathered, counting every occurrence */
  @Column(name = "command_count", nullable = false)
  private int commandCount;
}
