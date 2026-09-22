package cz.cyberrange.platform.training.api.dto;

import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelBasicDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Contains generally safe, descriptive-only data accessible by both organizers and trainees. */
@Data
@Schema(
    description = "The fields every level carries, whatever its type.",
    subTypes = {
      TrainingLevelBasicDTO.class,
      AccessLevelBasicDTO.class,
      InfoLevelBasicDTO.class,
      AssessmentLevelBasicDTO.class
    })
public abstract class AbstractLevelBasicDTO {

  @Schema(example = "1")
  protected Long id;

  @Schema(example = "Training Level1")
  protected String title;

  @Schema(example = "20")
  protected int maxScore;

  /** Zero-based position of the level within its training definition's sequence of levels */
  @Schema(description = "Zero-based position of the level within its definition.", example = "2")
  protected int order;

  @Schema(description = "Estimated time to finish the level, in minutes.", example = "5")
  protected int estimatedDuration;

  @Schema(example = "TRAINING_LEVEL")
  protected LevelType levelType;
}
