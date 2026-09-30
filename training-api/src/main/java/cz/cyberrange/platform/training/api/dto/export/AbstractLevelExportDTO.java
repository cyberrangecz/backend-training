package cz.cyberrange.platform.training.api.dto.export;

import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encapsulates information about abstract level. Extended by {@link AssessmentLevelExportDTO},
 * {@link TrainingLevelExportDTO}, {@link AccessLevelExportDTO} and {@link InfoLevelExportDTO}
 */
@Data
@NoArgsConstructor
@ApiModel(
    value = "AbstractLevelExportDTO",
    subTypes = {
      TrainingLevelExportDTO.class,
      AccessLevelExportDTO.class,
      InfoLevelExportDTO.class,
      AssessmentLevelExportDTO.class
    },
    description =
        "Superclass for classes TrainingLevelExportDTO, AccessLevelExportDTO, InfoLevelExportDTO and AssessmentLevelExportDTO")
public class AbstractLevelExportDTO {

  @ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
  protected String title;

  @ApiModelProperty(value = "Type of the level.", example = "TRAINING")
  protected LevelType levelType;

  @ApiModelProperty(
      value = "Estimated time (minutes) taken by the player to solve the level.",
      example = "5")
  protected int estimatedDuration;

  @ApiModelProperty(
      value =
          "Minimal possible solve time (minutes) that must be taken by the player to solve the level.",
      example = "5")
  protected Integer minimalPossibleSolveTime;
}
