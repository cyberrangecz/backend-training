package cz.cyberrange.platform.training.persistence.model.detection;

import cz.cyberrange.platform.training.persistence.model.AbstractEntity;
import cz.cyberrange.platform.training.persistence.model.enums.CommandType;
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
 * One recorded console command that matched a forbidden one, kept against the finding it counts
 * towards, with the machine it ran on and the moment it was entered
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "detected_forbidden_command")
@NamedQueries({
  @NamedQuery(
      name = "DetectedForbiddenCommand.findAllByEventId",
      query =
          "SELECT dfc FROM DetectedForbiddenCommand dfc "
              + "WHERE dfc.detectionEventId = :eventId"),
  @NamedQuery(
      name = "DetectedForbiddenCommand.deleteAllByDetectionEventId",
      query = "DELETE FROM DetectedForbiddenCommand dfc WHERE dfc.detectionEventId = :eventId")
})
public class DetectedForbiddenCommand extends AbstractEntity<Long> {

  /** The command line as it was recorded, not the forbidden text that matched it. */
  @Column(name = "command", nullable = false)
  private String command;

  /** The console of the forbidden command that matched. */
  @Column(name = "command_type", nullable = false)
  private CommandType type;

  @Column(name = "detection_event_id", nullable = false)
  private Long detectionEventId;

  @Column(name = "hostname")
  private String hostname;

  @Column(name = "occurred_at")
  private LocalDateTime occurredAt;
}
