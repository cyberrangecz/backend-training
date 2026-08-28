package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.betatestinggroup.BetaTestingGroupCreateDTO;
import cz.cyberrange.platform.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/** Encapsulates information about Training definition, intended for creation of new definition */
@Data
@ApiModel(value = "TrainingDefinitionCreateDTO", description = "Training definition to create.")
public class TrainingDefinitionCreateDTO {

  @ApiModelProperty(
      value = "A name of the training/game (e.g., Photo Hunter) .",
      required = true,
      example = "Photo Hunter")
  @NotEmpty(message = "{trainingDefinition.title.NotEmpty.message}")
  private String title;

  @ApiModelProperty(
      value = "Description of training definition that is visible to the participant.",
      example = "Description of Photo Hunter")
  private String description;

  @ApiModelProperty(
      value = "List of knowledge and skills necessary to complete the training.",
      example = "[HTML, http protocol]")
  private String[] prerequisites;

  @ApiModelProperty(
      value =
          "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ",
      example = "[outcomes]")
  private String[] outcomes;

  /** Stored as given; the service does not restrict which state a new definition may start in */
  @ApiModelProperty(
      value = "Current state of training definition.",
      required = true,
      example = "UNRELEASED")
  @NotNull(message = "{trainingDefinition.state.NotNull.message}")
  private TDState state;

  /**
   * When present, its organizer ids replace the group's membership on the created definition; when
   * absent, the definition is created without a beta testing group
   */
  @ApiModelProperty(
      value = "Group of organizers who is allowed to see the training definition.",
      required = true)
  @Valid
  private BetaTestingGroupCreateDTO betaTestingGroup;

  /** Governs whether the newly created definition is populated with a default set of levels */
  @ApiModelProperty(value = "Sign if default levels should be created.", example = "false")
  private boolean defaultContent;
}
