package cz.cyberrange.platform.training.api.dto.traininginstance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Encapsulates the information stating if training instance has finished */
@Data
@Schema(
    description =
        "Whether a training instance has finished, with a message to show before deleting it")
public class TrainingInstanceIsFinishedInfoDTO {

  @Schema(example = "false")
  private boolean hasFinished;

  @Schema(
      example = "WARNING: Training instance is still running! Are you sure you want to delete it?")
  private String message;
}
