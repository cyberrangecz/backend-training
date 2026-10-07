package cz.cyberrange.platform.training.api.dto.traininginstance;

import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about Training Instance */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "A training instance in full, with its training definition and access token")
public class TrainingInstanceDTO extends TrainingInstanceBasicDTO {

  /**
   * Mapped through {@code TrainingDefinitionMapper.mapToDTO}; its {@code canBeArchived} is never
   * patched afterward here, so it always carries that flag's default value of false
   */
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private TrainingDefinitionDTO trainingDefinition;

  /**
   * Carries the full token as stored on the instance, including the pin suffix the service appended
   * when it was generated
   */
  @Schema(
      description = "Full access token, including the pin the server appended to it.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "hunter-6578")
  private String accessToken;

  @Schema(example = "1")
  private Long poolId;

  @Schema(example = "2017-10-19T08:23:54.000Z")
  private LocalDateTime lastEdited;

  @Schema(example = "John Doe")
  private String lastEditedBy;

  @Schema(
      description =
          "True when sandboxes come from the sandbox definition locally instead of from a pool.",
      example = "true")
  private boolean localEnvironment;

  @Schema(example = "1")
  private Long sandboxDefinitionId;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
  private boolean showStepperBar;

  @Schema(example = "true")
  private boolean backwardMode;
}
