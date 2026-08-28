package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** Encapsulates basic information about Training Definition */
@Data
@ApiModel(
    value = "TrainingDefinitionInfoDTO",
    description = "Basic training definition information.")
public class TrainingDefinitionInfoDTO {

  @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
  private Long id;

  @ApiModelProperty(
      value = "A name of the training/game (e.g., Photo Hunter) .",
      example = "TrainingDefinition2")
  private String title;

  @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
  private TDState state;
}
