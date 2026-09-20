package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintForTrainingLevelViewDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The level as shown to a trainee currently solving it, on resuming a run or moving to the next
 * level. Carries no answer and no solution field, so a trainee playing the level cannot read either
 * through this shape.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "The training level as shown to the participant solving it.")
public class TrainingLevelViewDTO extends AbstractLevelDTO {

  @Schema(description = "The task presented to the participant.", example = "Play me")
  private String content;

  /** Whether requesting the solution reduces the score awardable for the level to zero */
  @Schema(
      description = "Whether showing the solution drops the level's score to zero.",
      example = "true")
  private boolean solutionPenalized;

  @Schema(description = "Estimated time to solve the level, in minutes.", example = "25")
  private int estimatedDuration;

  /**
   * Number of incorrect answer submissions allowed for the level, against which the number of
   * remaining attempts is calculated
   */
  @Schema(
      description = "How many wrong answers may be submitted before attempts run out.",
      example = "5")
  private int incorrectAnswerLimit;

  /** Hints as configured for the level, each with its title and point cost, advice withheld */
  private Set<HintForTrainingLevelViewDTO> hints = new HashSet<>();
}
