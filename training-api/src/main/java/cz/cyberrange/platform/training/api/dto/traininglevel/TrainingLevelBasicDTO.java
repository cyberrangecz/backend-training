package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintBasicDTO;
import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(
    value = "TrainingLevelBasicDTO",
    description = "An assignment containing security tasks whose completion yields a answer.",
    parent = AbstractLevelBasicDTO.class)
public class TrainingLevelBasicDTO extends AbstractLevelBasicDTO {
  /** Hints reduced to their title and point cost, with the advice text withheld */
  @ApiModelProperty(value = "Information which helps player resolve the level.")
  protected Set<HintBasicDTO> hints = new HashSet<>();

  /**
   * Number of incorrect answer submissions allowed for the level, against which the number of
   * remaining attempts is calculated
   */
  @ApiModelProperty(
      value = "How many times player can submit incorrect answer before displaying solution.",
      example = "5")
  protected int incorrectAnswerLimit;

  /** Whether requesting the solution reduces the score awardable for the level to zero */
  @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
  protected boolean solutionPenalized;

  @ApiModelProperty(value = "List of mitre techniques used in the training level.")
  protected List<MitreTechniqueDTO> mitreTechniques;
}
