package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Encapsulates the identification and description shared by all training definition responses */
@Data
@Schema(description = "The identification and description shared by every training definition.")
public abstract class AbstractTrainingDefinitionDTO {

  /** Primary key of the training definition row, distinct from any user id space */
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  protected Long id;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "TrainingDefinition2")
  protected String title;

  @Schema(example = "Unreleased training definition")
  protected String description;

  /**
   * Sum of the estimated durations of the definition's levels, maintained by the service as levels
   * are added, removed or edited; not accepted from a create or update request
   */
  @Schema(
      description = "Total time for the definition's levels in minutes, kept by the server.",
      example = "5")
  protected long estimatedDuration;
}
