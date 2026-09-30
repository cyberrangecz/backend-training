package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The access level content shown to a participant currently running it. Omits the passkey that
 * {@link AccessLevelDTO} carries. Its {@code localContent} always reaches the participant with
 * runtime placeholders substituted (the training instance access token, a bearer token, the
 * participant's user reference id, the sandbox definition id and the central syslog address);
 * {@code cloudContent} is left exactly as authored.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "AccessLevelViewDTO",
    description = "A level containing instructions on how to connect to the virtual machines.",
    parent = AbstractLevelDTO.class)
public class AccessLevelViewDTO extends AbstractLevelDTO {

  /**
   * Instructions for reaching the level's virtual machines from a cloud environment, as authored
   */
  @ApiModelProperty(
      value = "The instructions on how to connect to the machine in cloud environment.",
      example = "Connect using SSH config.")
  private String cloudContent;

  /**
   * Instructions for reaching the level's virtual machines from a local, non-cloud environment,
   * with its runtime placeholders already substituted for the requesting participant
   */
  @ApiModelProperty(
      value = "The instructions on how to connect to the machine in local (non-cloud) environment.",
      example = "Use vagrant SSH connection.")
  private String localContent;
}
