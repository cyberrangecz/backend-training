package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Payload for replacing an access level's passkey, cloud connection content and local connection
 * content. Carries the {@code ACCESS_LEVEL} discriminator that lets {@link AbstractLevelUpdateDTO}
 * resolve this subtype during deserialization.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "AccessLevelUpdateDTO", description = "Access level to update.")
public class AccessLevelUpdateDTO extends AbstractLevelUpdateDTO {

  /**
   * New value the participant must later submit to complete the level, replacing the stored one.
   */
  @ApiModelProperty(
      value = "Keyword found in training, used for access next level.",
      required = true,
      example = "secretAnswer")
  @Size(max = 50, message = "{accessLevel.passkey.Size.message}")
  @NotEmpty(message = "{accessLevel.passkey.NotEmpty.message}")
  private String passkey;

  /** New cloud-environment connection instructions, replacing the stored ones verbatim. */
  @ApiModelProperty(
      value = "The instructions on how to connect to the machine in cloud environment.",
      example = "Connect using SSH config.")
  @NotEmpty(message = "{accessLevel.cloudContent.NotEmpty.message}")
  private String cloudContent;

  /** New local, non-cloud connection instructions, replacing the stored ones verbatim. */
  @ApiModelProperty(
      value = "The instructions on how to connect to the machine in local (non-cloud) environment.",
      example = "Use vagrant SSH connection.")
  @NotEmpty(message = "{accessLevel.localContent.NotEmpty.message}")
  private String localContent;

  /** Sets the level type discriminator to access level. */
  public AccessLevelUpdateDTO() {
    this.levelType = LevelType.ACCESS_LEVEL;
  }
}
