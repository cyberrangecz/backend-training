package cz.cyberrange.platform.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encapsulates information about abstract level. Extended by {@link AssessmentLevelImportDTO},
 * {@link TrainingLevelImportDTO}, {@link AccessLevelImportDTO} and {@link InfoLevelImportDTO}.
 * Carries no order: a level takes its position from where it is created, so a submitted file naming
 * an order is accepted and that name is dropped. A subtype declaring {@link JsonIgnoreProperties}
 * of its own masks the one declared here and has to repeat {@code order}.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"order"})
@ApiModel(
    value = "AbstractLevelImportDTO",
    subTypes = {
      TrainingLevelImportDTO.class,
      AccessLevelImportDTO.class,
      InfoLevelImportDTO.class,
      AssessmentLevelImportDTO.class
    },
    description =
        "Superclass for classes TrainingLevelImportDTO, AccessLevelImportDTO, AssessmentLevelImportDTO and InfoLevelImportDTO")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "level_type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = TrainingLevelImportDTO.class, name = "TRAINING_LEVEL"),
  @JsonSubTypes.Type(value = TrainingLevelImportDTO.class, name = "GAME_LEVEL"),
  @JsonSubTypes.Type(value = AccessLevelImportDTO.class, name = "ACCESS_LEVEL"),
  @JsonSubTypes.Type(value = AssessmentLevelImportDTO.class, name = "ASSESSMENT_LEVEL"),
  @JsonSubTypes.Type(value = InfoLevelImportDTO.class, name = "INFO_LEVEL")
})
public class AbstractLevelImportDTO {

  @ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
  @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
  protected String title;

  /**
   * Selects, together with the JSON {@code level_type} discriminator, which concrete subtype is
   * deserialized and which entity type the level is imported as
   */
  @ApiModelProperty(value = "Type of the level.", example = "TRAINING_LEVEL")
  @NotNull(message = "{abstractLevel.type.NotNull.message}")
  protected LevelType levelType;

  /** Added with every other level's value into the imported training definition's own duration */
  @ApiModelProperty(
      value = "Estimated time (minutes) taken by the player to solve the level.",
      example = "5")
  @NotNull(message = "{abstractLevel.estimatedDuration.NotNull.message}")
  @Min(value = 0, message = "{abstractLevel.estimatedDuration.Min.message}")
  protected Integer estimatedDuration;

  @ApiModelProperty(
      value =
          "Minimal possible solve time (minutes) that must be taken by the player to solve the level.",
      example = "5")
  @NotNull(message = "{abstractLevel.minimalPossibleSolveTime.NotNull.message}")
  @Min(value = 0, message = "{abstractLevel.minimalPossibleSolveTime.Min.message}")
  protected Integer minimalPossibleSolveTime;
}
