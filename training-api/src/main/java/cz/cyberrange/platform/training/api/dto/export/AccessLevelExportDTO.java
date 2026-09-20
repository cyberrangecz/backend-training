package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Encapsulates information about training level. Inherits from {@link AbstractLevelExportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Schema(description = "An exported access level, telling the trainee how to reach the machines")
public class AccessLevelExportDTO extends AbstractLevelExportDTO {

  @Schema(description = "Text the trainee submits to complete the level", example = "secretAnswer")
  private String passkey;

  @Schema(
      description = "How to reach the machines in a cloud environment",
      example = "Connect using SSH config.")
  private String cloudContent;

  @Schema(
      description = "How to reach the machines in a local environment",
      example = "Use vagrant SSH connection.")
  private String localContent;
}
