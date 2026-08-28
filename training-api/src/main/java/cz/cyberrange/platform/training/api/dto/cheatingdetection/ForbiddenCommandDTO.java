package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import cz.cyberrange.platform.training.api.enums.CommandType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * A command a cheating detection run is configured to treat as forbidden. A recorded command counts
 * as a match only when it contains this command's text and was run in the same kind of shell.
 */
@Data
@ApiModel(value = "ForbiddenCommandDTO", description = "Basic information about forbidden command.")
public class ForbiddenCommandDTO {

  @ApiModelProperty(value = "command.", example = "nmap")
  private String command;

  @ApiModelProperty(value = "Type of command.", example = "BASH")
  private CommandType type;

  @ApiModelProperty(value = "Id of cheating detection.", example = "1")
  private Long cheatingDetectionId;
}
