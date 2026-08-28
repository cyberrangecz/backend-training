package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about Training Definition including its authoring and lifecycle data */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "TrainingDefinitionDTO", description = "A blueprint of abstract levels.")
public class TrainingDefinitionDTO extends AbstractTrainingDefinitionDTO {

  @ApiModelProperty(
      value = "List of knowledge and skills necessary to complete the training.",
      example = "")
  private String[] prerequisites;

  @ApiModelProperty(
      value =
          "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ",
      example = "")
  private String[] outcomes;

  @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
  private TDState state;

  /** Primary key of the associated beta testing group entity, not a user id */
  @ApiModelProperty(
      value = "Group of organizers who is allowed to see the training definition.",
      example = "14")
  private Long betaTestingGroupId;

  /**
   * Left unset by the mapper; the facade always assigns it afterward, from whether any of the
   * definition's training instances still ends in the future
   */
  @ApiModelProperty(
      value = "Sign if training definition can be archived or not.",
      example = "false")
  private boolean canBeArchived;

  /**
   * Overwritten with the current time by the service on every create or update, regardless of any
   * value supplied by the caller
   */
  @ApiModelProperty(
      value = "Time of last edit done to definition.",
      example = "2017-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime lastEdited;

  /**
   * Overwritten with the current user's full name by the service on every create or update,
   * regardless of any value supplied by the caller
   */
  @ApiModelProperty(
      value = "Name of the user who has done the last edit in definition.",
      example = "John Doe")
  private String lastEditedBy;

  /**
   * Stamped once when the definition is created and carried over unchanged on every later update
   */
  @ApiModelProperty(value = "Time of creation of definition.", example = "2017-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime createdAt;
}
