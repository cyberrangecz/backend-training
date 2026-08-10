package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** Encapsulates the identification and description shared by all training definition responses. */
@Data
public abstract class AbstractTrainingDefinitionDTO {

  @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
  protected Long id;

  @ApiModelProperty(
      value = "A name of the training/game (e.g., Photo Hunter) .",
      example = "TrainingDefinition2")
  protected String title;

  @ApiModelProperty(
      value = "Description of training definition that is visible to the participant.",
      example = "Unreleased training definition")
  protected String description;

  @ApiModelProperty(
      value = "Estimated time it takes to finish runs created from this definition.",
      example = "5")
  protected long estimatedDuration;
}
