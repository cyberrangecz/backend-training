package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A single console command captured as an audit event, together with the host, user, and working
 * directory it ran under
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "One console command a trainee ran, with where and as whom it ran.")
public class CommandEventDTO extends AbstractEventDTO {

  @Schema(example = "bash-command", description = "The kind of console command logged.")
  @JsonProperty("cmd_type")
  private String cmdType;

  // Holds only the executed program name; any arguments are held separately in commandArguments.
  @Schema(
      example = "ls",
      description = "The leading token of the logged command line, without its arguments.")
  private String command;

  @Schema(example = "-la")
  private String commandArguments;

  @Schema(example = "kali")
  private String hostname;

  @Schema(example = "root")
  private String username;

  @Schema(example = "/root", description = "Working directory the command ran from.")
  private String wd;

  @Schema(example = "10.0.0.1")
  private String ip;
}
