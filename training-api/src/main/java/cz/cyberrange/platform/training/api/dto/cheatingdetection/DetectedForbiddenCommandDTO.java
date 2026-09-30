package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import cz.cyberrange.platform.training.api.enums.CommandType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * One recorded console command that matched a forbidden one, kept with the machine it ran on and
 * the moment it was entered
 */
@Data
@Schema(description = "One recorded console command that matched a forbidden one.")
public class DetectedForbiddenCommandDTO {

  /** The command line as it was recorded, not the forbidden text that matched it */
  @Schema(
      example = "nmap",
      description = "The command line as recorded, not the forbidden text that matched it.")
  private String command;

  @Schema(example = "BASH")
  private CommandType type;

  @Schema(example = "attacker")
  private String hostname;

  @Schema(example = "2022-01-01T05:55:23")
  private LocalDateTime occurredAt;
}
