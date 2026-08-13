package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelBasicDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** Contains generally safe, descriptive-only data accessible by both organizers and trainees. */
@Data
@ApiModel(
    value = "AbstractLevelBasicDTO",
    subTypes = {
      TrainingLevelBasicDTO.class,
      AccessLevelBasicDTO.class,
      InfoLevelBasicDTO.class,
      AssessmentLevelBasicDTO.class
    },
    description =
        "Superclass for classes TrainingLevelBasicDTO, AccessLevelBasicDTO, AssessmentLevelBasicDTO and InfoLevelBasicDTO")
@JsonSubTypes({
  @JsonSubTypes.Type(value = TrainingLevelBasicDTO.class, name = "TrainingLevelBasicDTO"),
  @JsonSubTypes.Type(value = AccessLevelBasicDTO.class, name = "AccessLevelBasicDTO"),
  @JsonSubTypes.Type(value = AssessmentLevelBasicDTO.class, name = "AssessmentLevelBasicDTO"),
  @JsonSubTypes.Type(value = InfoLevelBasicDTO.class, name = "InfoLevelBasicDTO")
})
public abstract class AbstractLevelBasicDTO {

  @ApiModelProperty(value = "Main identifier of level.", example = "1")
  protected Long id;

  @ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
  protected String title;

  @ApiModelProperty(
      value = "The maximum score a participant can achieve during a level.",
      example = "20")
  protected int maxScore;

  @ApiModelProperty(value = "Order of level, starts with 0", example = "2")
  protected int order;

  @ApiModelProperty(
      value = "Estimated time taken by the player to resolve the level.",
      example = "5")
  protected int estimatedDuration;

  @ApiModelProperty(value = "Type of the level.", example = "TRAINING")
  protected LevelType levelType;
}
