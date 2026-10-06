package cz.cyberrange.platform.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Schema(
    description = "One level of a training definition being imported; a submitted order is ignored",
    discriminatorProperty = "level_type",
    discriminatorMapping = {
      @DiscriminatorMapping(value = "TRAINING_LEVEL", schema = TrainingLevelImportDTO.class),
      @DiscriminatorMapping(value = "GAME_LEVEL", schema = TrainingLevelImportDTO.class),
      @DiscriminatorMapping(value = "ACCESS_LEVEL", schema = AccessLevelImportDTO.class),
      @DiscriminatorMapping(value = "ASSESSMENT_LEVEL", schema = AssessmentLevelImportDTO.class),
      @DiscriminatorMapping(value = "INFO_LEVEL", schema = InfoLevelImportDTO.class)
    },
    subTypes = {
      TrainingLevelImportDTO.class,
      AccessLevelImportDTO.class,
      InfoLevelImportDTO.class,
      AssessmentLevelImportDTO.class
    })
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

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Training Level1")
  @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
  protected String title;

  /**
   * Selects, together with the JSON {@code level_type} discriminator, which concrete subtype is
   * deserialized and which entity type the level is imported as
   */
  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Chooses the kind of level created, alongside the level_type discriminator",
      example = "TRAINING_LEVEL")
  @NotNull(message = "{abstractLevel.type.NotNull.message}")
  protected LevelType levelType;

  /** Added with every other level's value into the imported training definition's own duration */
  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description =
          "Time the level is expected to take, in minutes, summed into the definition's estimated"
              + " duration",
      example = "5")
  @NotNull(message = "{abstractLevel.estimatedDuration.NotNull.message}")
  @Min(value = 0, message = "{abstractLevel.estimatedDuration.Min.message}")
  protected Integer estimatedDuration;

  @Schema(
      description = "Threshold in minutes below which a correct answer is flagged as cheating",
      example = "5")
  @Min(value = 0, message = "{abstractLevel.minimalPossibleSolveTime.Min.message}")
  protected Integer minimalPossibleSolveTime;
}
