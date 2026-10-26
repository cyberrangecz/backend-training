package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelCreateDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A level of a training definition being created. Implemented by {@link TrainingLevelCreateDTO},
 * {@link AccessLevelCreateDTO}, {@link AssessmentLevelCreateDTO} and {@link InfoLevelCreateDTO}.
 *
 * <p>Deserializing a value declared as this type picks the concrete subtype by reading the {@code
 * level_type} property that must already be present in the payload, matching it against one of the
 * names below. That property is the JSON key the request body carries for the {@code levelType}
 * field, once translated through the service's snake-case property naming strategy.
 *
 * <p>A level carries no id, because the server assigns it: a payload giving an {@code id} a value
 * fails to deserialize, while an {@code id} of {@code null} is accepted.
 */
@Schema(
    description = "The fields every level of a new training definition carries, whatever its type.",
    subTypes = {
      TrainingLevelCreateDTO.class,
      AccessLevelCreateDTO.class,
      AssessmentLevelCreateDTO.class,
      InfoLevelCreateDTO.class
    },
    discriminatorProperty = "level_type",
    discriminatorMapping = {
      @DiscriminatorMapping(value = "TRAINING_LEVEL", schema = TrainingLevelCreateDTO.class),
      @DiscriminatorMapping(value = "GAME_LEVEL", schema = TrainingLevelCreateDTO.class),
      @DiscriminatorMapping(value = "ACCESS_LEVEL", schema = AccessLevelCreateDTO.class),
      @DiscriminatorMapping(value = "ASSESSMENT_LEVEL", schema = AssessmentLevelCreateDTO.class),
      @DiscriminatorMapping(value = "INFO_LEVEL", schema = InfoLevelCreateDTO.class)
    })
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "level_type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = TrainingLevelCreateDTO.class, name = "TRAINING_LEVEL"),
  @JsonSubTypes.Type(value = TrainingLevelCreateDTO.class, name = "GAME_LEVEL"),
  @JsonSubTypes.Type(value = AccessLevelCreateDTO.class, name = "ACCESS_LEVEL"),
  @JsonSubTypes.Type(value = AssessmentLevelCreateDTO.class, name = "ASSESSMENT_LEVEL"),
  @JsonSubTypes.Type(value = InfoLevelCreateDTO.class, name = "INFO_LEVEL")
})
public interface AbstractLevelCreateDTO {

  /**
   * Names the level type the payload carries, the value the {@code level_type} discriminator
   * selects the concrete subtype by.
   *
   * @return the payload's level type
   */
  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Selects the payload's level type; GAME_LEVEL is read as TRAINING_LEVEL.",
      example = "TRAINING_LEVEL")
  LevelType getLevelType();

  /**
   * Receives every JSON property no field of the payload declares. A level carries no id of its
   * own, so an {@code id} with a value is refused; an {@code id} of {@code null} and any other
   * undeclared property are dropped.
   *
   * @param propertyName the JSON property name
   * @param ignoredValue the value the property carried
   * @throws IllegalArgumentException when the property is {@code id} and carries a value
   */
  @JsonAnySetter
  default void rejectIdentifier(String propertyName, Object ignoredValue) {
    if ("id".equals(propertyName) && ignoredValue != null) {
      throw new IllegalArgumentException(
          "A level of a new training definition carries no id; the server assigns it.");
    }
  }
}
