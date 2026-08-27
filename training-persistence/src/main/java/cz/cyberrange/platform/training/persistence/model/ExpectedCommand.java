package cz.cyberrange.platform.training.persistence.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/** Holds a single console command string recorded for a training level. */
@Getter
@Setter
@Embeddable
public class ExpectedCommand {

  @Column(name = "command")
  private String command;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExpectedCommand that = (ExpectedCommand) o;
    return Objects.equals(getCommand(), that.getCommand());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCommand());
  }

  @Override
  public String toString() {
    return "ExpectedCommand{" + "command='" + command + '\'' + '}';
  }
}
