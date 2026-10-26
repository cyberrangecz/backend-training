package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelContentDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Holds the passkey and connection details of an access level in a request. Extended by {@link
 * AccessLevelCreateDTO} and {@link AccessLevelUpdateDTO}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AccessLevelContentDTO extends AbstractLevelContentDTO {

  /** Value the participant must submit to complete the level */
  @Schema(
      description = "The value a participant must submit to finish the level.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "secretAnswer")
  @Size(max = 50, message = "{accessLevel.passkey.Size.message}")
  @NotEmpty(message = "{accessLevel.passkey.NotEmpty.message}")
  private String passkey;

  /** Instructions for reaching the level's virtual machines from a cloud environment */
  @Schema(
      description = "How to reach the level's virtual machines from a cloud environment.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "Connect using SSH config.")
  @NotEmpty(message = "{accessLevel.cloudContent.NotEmpty.message}")
  private String cloudContent;

  /** Instructions for reaching the level's virtual machines from a local environment */
  @Schema(
      description = "How to reach the level's virtual machines from a local environment.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "Use vagrant SSH connection.")
  @NotEmpty(message = "{accessLevel.localContent.NotEmpty.message}")
  private String localContent;

  /** Sets the level type discriminator to access level */
  protected AccessLevelContentDTO() {
    this.levelType = LevelType.ACCESS_LEVEL;
  }
}
