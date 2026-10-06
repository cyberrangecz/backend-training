package cz.cyberrange.platform.training.persistence.model.detection;

import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A finding that a level marked as requiring console commands was solved without any command being
 * recorded for it, its solution not having been revealed. It adds no evidence beyond what every
 * finding carries.
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@Entity
@Table(name = "no_commands_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
  @NamedQuery(
      name = "NoCommandsDetectionEvent.findNoCommandsEventById",
      query = "SELECT ncde FROM NoCommandsDetectionEvent ncde WHERE ncde.id = :eventId"),
  @NamedQuery(
      name = "NoCommandsDetectionEvent.findAllByCheatingDetectionId",
      query =
          "SELECT ncde FROM NoCommandsDetectionEvent ncde WHERE ncde.cheatingDetectionId = :cheatingDetectionId")
})
public class NoCommandsDetectionEvent extends AbstractDetectionEvent {}
