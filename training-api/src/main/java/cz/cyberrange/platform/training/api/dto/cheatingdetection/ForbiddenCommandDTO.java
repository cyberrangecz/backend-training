package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import cz.cyberrange.platform.training.api.enums.CommandType;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * A command a cheating detection run is configured to treat as forbidden. A recorded command counts
 * as a match only when it contains this command's text and was run in the same kind of shell.
 */
@Data
@Schema(
    description =
        "A command a detection run forbids, matched inside any command of the same console.")
public class ForbiddenCommandDTO {

  @NotBlank
  @Schema(example = "nmap")
  private String command;

  @NotNull
  @Schema(example = "BASH")
  private CommandType type;

  @Schema(example = "1")
  private Long cheatingDetectionId;
}
