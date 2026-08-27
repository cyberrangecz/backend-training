package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
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
 * A level built around an assignment whose completion requires submitting a correct answer. Carries
 * the task text, the stored answer, and the solution in full; returned only when reading a training
 * definition's levels, never on a trainee's run.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "TrainingLevelDTO",
    description = "An assignment containing security tasks whose completion yields a answer.",
    parent = AbstractLevelDTO.class)
public class TrainingLevelDTO extends AbstractLevelDTO {

  /**
   * The literal keyword a submission is compared against when the level's answer is not variant;
   * ignored in favor of a value resolved per trainee from the external answer storage service
   * otherwise.
   */
  @ApiModelProperty(
      value = "Keyword found in training, used for access next level.",
      example = "secretAnswer")
  private String answer;

  /**
   * Key used to look up the answer's value in the external answer storage service when the level's
   * answer is variant.
   */
  @ApiModelProperty(
      value = "Identifier that is used to obtain answer from remote storage.",
      example = "username")
  private String answerVariableName;

  /** The task description presented to the participant while attempting the level. */
  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Play me")
  private String content;

  /**
   * The solution text as authored for the level, carried here without regard to whether any trainee
   * has actually displayed it.
   */
  @ApiModelProperty(
      value = "Instruction how to get answer in training.",
      example = "This is how you do it")
  private String solution;

  /** Whether requesting the solution reduces the score awardable for the level to zero. */
  @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
  private boolean solutionPenalized;

  @ApiModelProperty(value = "Information which helps player resolve the level.")
  private Set<HintDTO> hints = new HashSet<>();

  /**
   * Number of incorrect answer submissions allowed for the level, against which the number of
   * remaining attempts is calculated.
   */
  @ApiModelProperty(
      value = "How many times player can submit incorrect answer before displaying solution.",
      example = "5")
  private int incorrectAnswerLimit;

  /**
   * Whether each trainee's answer is resolved from the external answer storage service, keyed by
   * the answer variable name, instead of taken from the literal answer field.
   */
  @ApiModelProperty(
      value =
          "Marking if flags/answers are randomly generated and are different for each trainee. Default is false.",
      example = "false")
  private boolean variantAnswers;

  @ApiModelProperty(value = "List of mitre techniques used in the training level.")
  private List<MitreTechniqueDTO> mitreTechniques;

  @ApiModelProperty(
      value = "Set of the expected commands to be executed during the training level.")
  private Set<String> expectedCommands;

  /** Whether the run's cheat-detection check requires at least one submitted command. */
  @ApiModelProperty(
      value =
          "Indicates if at least one command has to be executed to complete the level. Default is true.",
      example = "true")
  private boolean commandsRequired;

  /**
   * Minimum time, in minutes, a trainee is expected to take on the level; the run's cheat-detection
   * check compares it, converted to seconds, against the elapsed submission time.
   */
  @ApiModelProperty(
      value =
          "Minimal possible solve time (minutes) that must be taken by the player to solve the level.",
      example = "5")
  private Integer minimalPossibleSolveTime;
}
