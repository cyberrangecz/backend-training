package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A training level submitted by a training designer to create or replace one. Every field present
 * here is written onto the entity unconditionally, so a field left out of the request clears
 * whatever the entity previously held for it.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "TrainingLevelUpdateDTO", description = "Training level to update.")
public class TrainingLevelUpdateDTO extends AbstractLevelUpdateDTO {

  @ApiModelProperty(
      value = "The maximum score a participant can achieve during a level.",
      required = true,
      example = "20")
  @NotNull(message = "{trainingLevel.maxScore.NotNull.message}")
  @Min(value = 0, message = "{trainingLevel.maxScore.Min.message}")
  @Max(value = 100, message = "{trainingLevel.maxScore.Max.message}")
  private int maxScore;

  /** A blank submission is converted to a null answer when the entity is built */
  @ApiModelProperty(
      value = "Keyword found in training, used for access next level.",
      required = true,
      example = "secretAnswer")
  @Size(max = 50, message = "{trainingLevel.answer.Size.message}")
  private String answer;

  /** A blank submission is converted to a null answer variable name when the entity is built */
  @ApiModelProperty(
      value = "Identifier that is used to obtain answer from remote storage.",
      example = "username")
  @Size(max = 50, message = "{trainingLevel.answerVariableName.Size.message}")
  private String answerVariableName;

  @ApiModelProperty(
      value = "The information and experiences that are directed towards an player.",
      example = "Play me")
  @NotEmpty(message = "{trainingLevel.content.NotEmpty.message}")
  private String content;

  @ApiModelProperty(
      value = "Instruction how to get answer in training.",
      example = "This is how you do it")
  @NotEmpty(message = "{trainingLevel.solution.NotEmpty.message}")
  private String solution;

  /** Whether requesting the solution reduces the score awardable for the level to zero */
  @ApiModelProperty(
      value = "Sign if displaying of solution is penalized.",
      required = true,
      example = "false")
  @NotNull(message = "{trainingLevel.solutionPenalized.NotNull.message}")
  private boolean solutionPenalized;

  @ApiModelProperty(
      value = "Estimated time (minutes) taken by the player to solve the level.",
      example = "20")
  private int estimatedDuration;

  /**
   * Number of incorrect answer submissions allowed for the level, against which the number of
   * remaining attempts is calculated
   */
  @ApiModelProperty(
      value = "How many times participant can submit incorrect answer before displaying solution.",
      required = true,
      example = "5")
  @NotNull(message = "{trainingLevel.incorrectAnswerLimit.NotNull.message}")
  @Min(value = 0, message = "{trainingLevel.incorrectAnswerLimit.Min.message}")
  @Max(value = 100, message = "{trainingLevel.incorrectAnswerLimit.Max.message}")
  private int incorrectAnswerLimit;

  @Valid
  @ApiModelProperty(value = "Information which helps participant resolve the level.")
  private Set<HintDTO> hints = new HashSet<>();

  /**
   * Whether each trainee's answer is resolved from the external answer storage service, keyed by
   * the answer variable name, instead of taken from the literal answer field
   */
  @ApiModelProperty(
      value =
          "Indicates if flags/answers are randomly generated and are different for each trainee. Default is false.",
      example = "false")
  private boolean variantAnswers;

  @Valid
  @ApiModelProperty(value = "List of mitre techniques used in the training level.")
  private List<MitreTechniqueDTO> mitreTechniques;

  @ApiModelProperty(
      value = "Set of the expected commands to be executed during the training level.")
  private Set<String> expectedCommands;

  /**
   * Minimum time, in minutes, a trainee is expected to take on the level; the run's cheat-detection
   * check compares it, converted to seconds, against the elapsed submission time
   */
  @ApiModelProperty(
      value =
          "Minimal possible solve time (minutes) that must be taken by the player to solve the level.",
      example = "5")
  protected Integer minimalPossibleSolveTime;

  /**
   * Whether the run's cheat-detection check requires at least one submitted command for the level.
   * A request that omits this field deserializes it as false, clearing the entity's default of true
   * rather than leaving it at true.
   */
  @ApiModelProperty(
      value =
          "Indicates if at least one command has to be executed to complete the level. Default is true.",
      example = "true")
  private boolean commandsRequired;

  /** Sets the level type discriminator to training level */
  public TrainingLevelUpdateDTO() {
    this.levelType = LevelType.TRAINING_LEVEL;
  }
}
