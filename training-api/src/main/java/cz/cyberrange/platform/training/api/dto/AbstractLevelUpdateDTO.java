package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Schema(
    description = "The fields every level update carries, whatever the level type.",
    subTypes = {
      TrainingLevelUpdateDTO.class,
      AccessLevelUpdateDTO.class,
      AssessmentLevelUpdateDTO.class,
      InfoLevelUpdateDTO.class
    },
    discriminatorProperty = "level_type",
    discriminatorMapping = {
      @DiscriminatorMapping(value = "TRAINING_LEVEL", schema = TrainingLevelUpdateDTO.class),
      @DiscriminatorMapping(value = "GAME_LEVEL", schema = TrainingLevelUpdateDTO.class),
      @DiscriminatorMapping(value = "ACCESS_LEVEL", schema = AccessLevelUpdateDTO.class),
      @DiscriminatorMapping(value = "ASSESSMENT_LEVEL", schema = AssessmentLevelUpdateDTO.class),
      @DiscriminatorMapping(value = "INFO_LEVEL", schema = InfoLevelUpdateDTO.class)
    })
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

  @Schema(
      description = "Identifies the level to update within the definition.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "1")
  @NotNull(message = "{abstractLevel.id.NotNull.message}")
  protected Long id;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Training Level1")
  @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
  protected String title;

  @Schema(
      description = "Selects the payload's level type; GAME_LEVEL is read as TRAINING_LEVEL.",
      example = "TRAINING_LEVEL")
  protected LevelType levelType;
}
