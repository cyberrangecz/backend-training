package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "New passkey and connection details to store on an access level.")
public class AccessLevelUpdateDTO extends AbstractLevelUpdateDTO {

  /** New value the participant must later submit to complete the level, replacing the stored one */
  @Schema(
      description = "The value a participant must submit to finish the level.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "secretAnswer")
  @Size(max = 50, message = "{accessLevel.passkey.Size.message}")
  @NotEmpty(message = "{accessLevel.passkey.NotEmpty.message}")
  private String passkey;

  /** New cloud-environment connection instructions, replacing the stored ones verbatim */
  @Schema(
      description = "How to reach the level's virtual machines from a cloud environment.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "Connect using SSH config.")
  @NotEmpty(message = "{accessLevel.cloudContent.NotEmpty.message}")
  private String cloudContent;

  /** New local, non-cloud connection instructions, replacing the stored ones verbatim */
  @Schema(
      description = "How to reach the level's virtual machines from a local environment.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "Use vagrant SSH connection.")
  @NotEmpty(message = "{accessLevel.localContent.NotEmpty.message}")
  private String localContent;

  /** Sets the level type discriminator to access level */
  public AccessLevelUpdateDTO() {
    this.levelType = LevelType.ACCESS_LEVEL;
  }
}
