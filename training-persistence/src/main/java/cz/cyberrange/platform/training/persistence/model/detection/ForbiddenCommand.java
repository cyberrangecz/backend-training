package cz.cyberrange.platform.training.persistence.model.detection;

import cz.cyberrange.platform.training.persistence.model.AbstractEntity;
import cz.cyberrange.platform.training.persistence.model.enums.CommandType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A command one cheating detection sweep treats as forbidden. A recorded command matches only when
 * it contains this text and was entered in this console, so the two columns narrow the match
 * together.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "forbidden_command")
public class ForbiddenCommand extends AbstractEntity<Long> {

  /** Matched as a substring of a recorded command line, not as the whole of it */
  @Column(name = "command", nullable = false)
  private String command;

  /** The console the command must have been entered in for a match to count */
  @Column(name = "command_type", nullable = false)
  private CommandType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cheating_detection_id")
  private CheatingDetection cheatingDetection;
}
