package cz.cyberrange.platform.training.api.dto.export;

import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encapsulates information about abstract level. Extended by {@link AssessmentLevelExportDTO},
 * {@link TrainingLevelExportDTO}, {@link AccessLevelExportDTO} and {@link InfoLevelExportDTO}
 */
@Data
@NoArgsConstructor
@Schema(
    description = "One level of an exported training definition",
    subTypes = {
      TrainingLevelExportDTO.class,
      AccessLevelExportDTO.class,
      InfoLevelExportDTO.class,
      AssessmentLevelExportDTO.class
    })
public class AbstractLevelExportDTO {

  @Schema(example = "Training Level1")
  protected String title;

  @Schema(example = "TRAINING_LEVEL")
  protected LevelType levelType;

  @Schema(description = "Time the level is expected to take, in minutes", example = "5")
  protected int estimatedDuration;

  @Schema(
      description = "Threshold in minutes below which a correct answer is flagged as cheating",
      example = "5")
  protected Integer minimalPossibleSolveTime;
}
