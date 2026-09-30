package cz.cyberrange.platform.training.api.dto.hint;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A hint a training run has already taken, carrying the advice as it was recorded on the run rather
 * than as the level currently defines it. The penalty is not carried, having been applied when the
 * hint was taken. Two of these compare equal on identity, name and advice alone, disregarding
 * position.
 */
@Data
@Schema(description = "A hint already taken in a run, with the advice as recorded then.")
public class TakenHintDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "Hint1")
  private String title;

  @Schema(
      description = "The advice as it was recorded when the hint was taken.",
      example = "Very good advice")
  private String content;

  @EqualsAndHashCode.Exclude
  @Schema(description = "Position of the hint within its level.", example = "1")
  private int order;
}
