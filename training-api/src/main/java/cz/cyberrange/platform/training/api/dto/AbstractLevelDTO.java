package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Encapsulates information about abstract level. Extended by {@link AssessmentLevelDTO}, {@link
 * TrainingLevelDTO}, {@link AccessLevelDTO} and {@link InfoLevelDTO}
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(
    value = "AbstractLevelDTO",
    subTypes = {
      TrainingLevelDTO.class,
      AccessLevelDTO.class,
      InfoLevelDTO.class,
      AssessmentLevelDTO.class
    },
    description =
        "Superclass for classes TrainingLevelDTO, AccessLevelDTO, AssessmentLevelDTO and InfoLevelDTO")
@JsonSubTypes({
  @JsonSubTypes.Type(value = TrainingLevelDTO.class, name = "TrainingLevelDTO"),
  @JsonSubTypes.Type(value = AccessLevelDTO.class, name = "AccessLevelDTO"),
  @JsonSubTypes.Type(value = AssessmentLevelDTO.class, name = "AssessmentLevelDTO"),
  @JsonSubTypes.Type(value = InfoLevelDTO.class, name = "InfoLevelDTO")
})
public abstract class AbstractLevelDTO extends AbstractLevelBasicDTO {

  @ApiModelProperty(value = "Training definition to which is this level assigned.", example = "2")
  protected TrainingDefinitionDTO trainingDefinition;
}
