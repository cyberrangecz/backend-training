package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.hint.TakenHintDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A previously visited level replayed back to the trainee who visited it, as part of a training run
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "A training level replayed to the participant who already visited it.")
public class TrainingLevelPreviewDTO extends AbstractLevelDTO {

  @Schema(description = "The task as presented to the participant.", example = "Play me")
  private String content;

  /** Hints this trainee actually took while visiting the level, rebuilt from their run history */
  private Set<TakenHintDTO> hints = new HashSet<>();

  /**
   * The solution as recorded for this trainee's run, with any embedded answer placeholder resolved
   * to their own answer; null if this trainee has not displayed it
   */
  @Schema(
      description = "The solution as this participant saw it; unset if they never showed it.",
      example = "This is how you do it")
  private String solution;
}
