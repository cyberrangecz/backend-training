package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * Encapsulates the fields common to every level update payload. Extended by {@link
 * TrainingLevelUpdateDTO}, {@link AccessLevelUpdateDTO}, {@link AssessmentLevelUpdateDTO} and
 * {@link InfoLevelUpdateDTO}.
 *
 * <p>Deserializing a value declared as this type picks the concrete subtype by reading the {@code
 * level_type} property that must already be present in the payload, matching it against one of the
 * names below. That property is the JSON key the request body carries for the {@code levelType}
 * field, once translated through the service's snake-case property naming strategy.
 */
@Data
@ApiModel(
    value = "AbstractLevelUpdateDTO",
    subTypes = {
      TrainingLevelUpdateDTO.class,
      AccessLevelUpdateDTO.class,
      AssessmentLevelUpdateDTO.class,
      InfoLevelUpdateDTO.class
    },
    description =
        "Superclass for classes TrainingLevelUpdateDTO, AccessLevelUpdateDTO, AssessmentLevelUpdateDTO and InfoLevelUpdateDTO")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "level_type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = TrainingLevelUpdateDTO.class, name = "TRAINING_LEVEL"),
  @JsonSubTypes.Type(value = TrainingLevelUpdateDTO.class, name = "GAME_LEVEL"),
  @JsonSubTypes.Type(value = AccessLevelUpdateDTO.class, name = "ACCESS_LEVEL"),
  @JsonSubTypes.Type(value = AssessmentLevelUpdateDTO.class, name = "ASSESSMENT_LEVEL"),
  @JsonSubTypes.Type(value = InfoLevelUpdateDTO.class, name = "INFO_LEVEL")
})
public abstract class AbstractLevelUpdateDTO {

  @ApiModelProperty(value = "Main identifier of level.", required = true, example = "1")
  @NotNull(message = "{abstractLevel.id.NotNull.message}")
  protected Long id;

  @ApiModelProperty(
      value = "Short textual description of the level.",
      required = true,
      example = "Training Level1")
  @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
  protected String title;

  @ApiModelProperty(value = "Type of the level.", example = "TRAINING_LEVEL")
  protected LevelType levelType;
}
