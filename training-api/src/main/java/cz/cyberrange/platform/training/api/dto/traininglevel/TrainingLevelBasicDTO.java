package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintBasicDTO;
import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A training level reduced to what a bulk level listing or a score report needs: no task text, no
 * answer, and no solution
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "A training level in outline, without its task, answer or solution.")
public class TrainingLevelBasicDTO extends AbstractLevelBasicDTO {
  /** Hints reduced to their title and point cost, with the advice text withheld */
  protected Set<HintBasicDTO> hints = new HashSet<>();

  /**
   * Number of incorrect answer submissions allowed for the level, against which the number of
   * remaining attempts is calculated
   */
  @Schema(
      description = "How many wrong answers may be submitted before attempts run out.",
      example = "5")
  protected int incorrectAnswerLimit;

  /** Whether requesting the solution reduces the score awardable for the level to zero */
  @Schema(
      description = "Whether showing the solution drops the level's score to zero.",
      example = "true")
  protected boolean solutionPenalized;

  protected List<MitreTechniqueDTO> mitreTechniques;
}
