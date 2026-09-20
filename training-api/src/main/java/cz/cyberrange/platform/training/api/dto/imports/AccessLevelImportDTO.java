package cz.cyberrange.platform.training.api.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about an access level. Inherits from {@link AbstractLevelImportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An access level to create, telling the trainee how to reach the machines")
public class AccessLevelImportDTO extends AbstractLevelImportDTO {

  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Text the trainee submits to complete the level",
      example = "secretAnswer")
  @Size(max = 50, message = "{accessLevel.passkey.Size.message}")
  @NotEmpty(message = "{accessLevel.passkey.NotEmpty.message}")
  private String passkey;

  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "How to reach the machines in a cloud environment",
      example = "Connect using SSH config.")
  @NotEmpty(message = "{accessLevel.cloudContent.NotEmpty.message}")
  private String cloudContent;

  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "How to reach the machines in a local environment",
      example = "Use vagrant SSH connection.")
  @NotEmpty(message = "{accessLevel.localContent.NotEmpty.message}")
  private String localContent;
}
