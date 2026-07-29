package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingDefinitionBasicDTO", description = "A blueprint of abstract levels.")
public class TrainingDefinitionBasicDTO {
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

  @ApiModelProperty(value = "All levels in the training definition, ordered by their order.")
  protected List<AbstractLevelBasicDTO> levels = new ArrayList<>();
}
