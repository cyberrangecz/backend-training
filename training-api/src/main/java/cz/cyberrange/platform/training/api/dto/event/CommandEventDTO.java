package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString(callSuper = true)
@ApiModel(value = "CommandEventDTO", description = "Command event from training run")
public class CommandEventDTO extends AbstractEventDTO {

  @ApiModelProperty(value = "Command type", example = "bash-command")
  @JsonProperty("cmd_type")
  private String cmdType;

  @ApiModelProperty(value = "Executed command", example = "ls")
  private String command;

  @ApiModelProperty(value = "Command arguments", example = "-la")
  private String commandArguments;

  @ApiModelProperty(value = "Hostname of the machine", example = "kali")
  private String hostname;

  @ApiModelProperty(value = "Username of the user", example = "root")
  private String username;

  @ApiModelProperty(value = "Working directory", example = "/root")
  private String wd;

  @ApiModelProperty(value = "IP address of the machine", example = "10.0.0.1")
  private String ip;
}
