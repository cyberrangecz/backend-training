package cz.cyberrange.platform.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Encapsulates information about training definition and its levels. Carries no state and no
 * estimated duration: both are decided when the definition is created
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "ImportTrainingDefinitionDTO", description = "A basic information about hint.")
@JsonIgnoreProperties({"show_stepper_bar", "variant_sandboxes", "state", "estimated_duration"})
public class ImportTrainingDefinitionDTO {

  @ApiModelProperty(
      value = "A name of the training/game (e.g., Photo Hunter) .",
      example = "TrainingDefinition2")
  @NotEmpty(message = "{trainingDefinition.title.NotEmpty.message}")
  private String title;

  @ApiModelProperty(
      value = "Description of training definition that is visible to the participant.",
      example = "Unreleased training definition")
  @NotNull(message = "{trainingDefinition.description.NotNull.message}")
  private String description;

  @ApiModelProperty(
      value = "List of knowledge and skills necessary to complete the training.",
      example = "")
  private String[] prerequisites;

  @ApiModelProperty(
      value =
          "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ",
      example = "")
  private String[] outcomes;

  @Valid
  @ApiModelProperty(value = "Information about all levels in training definition.")
  private List<AbstractLevelImportDTO> levels = new ArrayList<>();

  /**
   * Replaces the level list with a new list holding the same elements, so later mutation of the
   * argument list does not affect this instance.
   *
   * @param levels the list of {@link AbstractLevelImportDTO}
   */
  public void setLevels(List<AbstractLevelImportDTO> levels) {
    this.levels = new ArrayList<>(levels);
  }
}
