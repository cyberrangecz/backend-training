package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Encapsulates information about Training Definition */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingDefinitionDTO", description = "A blueprint of abstract levels.")
public class TrainingDefinitionDTO extends TrainingDefinitionBasicDTO {

  @ApiModelProperty(
      value = "List of knowledge and skills necessary to complete the training.",
      example = "")
  private String[] prerequisites;

  @ApiModelProperty(
      value =
          "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ",
      example = "")
  private String[] outcomes;

  @ApiModelProperty(
      value = "Group of organizers who is allowed to see the training definition.",
      example = "14")
  private Long betaTestingGroupId;

  @ApiModelProperty(
      value = "Sign if training definition can be archived or not.",
      example = "false")
  private boolean canBeArchived;

  @ApiModelProperty(
      value = "Time of last edit done to definition.",
      example = "2017-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime lastEdited;

  @ApiModelProperty(
      value = "Name of the user who has done the last edit in definition.",
      example = "John Doe")
  private String lastEditedBy;

  @ApiModelProperty(value = "Time of creation of definition.", example = "2017-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime createdAt;
}
