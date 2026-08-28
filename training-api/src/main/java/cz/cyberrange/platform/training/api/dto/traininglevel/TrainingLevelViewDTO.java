package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintForTrainingLevelViewDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(
    value = "TrainingLevelViewDTO",
    description = "An assignment containing security tasks whose completion yields a answer.",
    parent = AbstractLevelDTO.class)
public class TrainingLevelViewDTO extends AbstractLevelDTO {

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Play me")
  private String content;

  /** Whether requesting the solution reduces the score awardable for the level to zero */
  @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
  private boolean solutionPenalized;

  @ApiModelProperty(
      value = "Estimated time (minutes) taken by the player to solve the level.",
      example = "25")
  private int estimatedDuration;

  /**
   * Number of incorrect answer submissions allowed for the level, against which the number of
   * remaining attempts is calculated
   */
  @ApiModelProperty(
      value = "How many times player can submit incorrect answer before displaying solution.",
      example = "5")
  private int incorrectAnswerLimit;

  /** Hints as configured for the level, each with its title and point cost, advice withheld */
  @ApiModelProperty(value = "Information which helps player resolve the level.")
  private Set<HintForTrainingLevelViewDTO> hints = new HashSet<>();
}
