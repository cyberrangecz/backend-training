package cz.cyberrange.platform.training.api.dto.hint;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * A hint as it appears in a training level being viewed rather than played: its name, its position
 * and what taking it would cost, with the advice itself withheld
 */
@Data
@Schema(description = "A hint offered on the level being played, with the advice withheld.")
public class HintForTrainingLevelViewDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "Hint1")
  private String title;

  @Schema(description = "Points taken off the score once the hint is taken.", example = "10")
  private Integer hintPenalty;

  @Schema(description = "Position of the hint within its level.", example = "1")
  private int order;
}
