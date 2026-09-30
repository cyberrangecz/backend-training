package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
    description = "The content stored on a training level; a field left out clears what was there.")
public class TrainingLevelUpdateDTO extends AbstractLevelUpdateDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
  @NotNull(message = "{trainingLevel.maxScore.NotNull.message}")
  @Min(value = 0, message = "{trainingLevel.maxScore.Min.message}")
  @Max(value = 100, message = "{trainingLevel.maxScore.Max.message}")
  private int maxScore;

  /** A blank submission is converted to a null answer when the entity is built */
  @Schema(
      description = "The keyword a submission must match; blank stores no answer.",
      example = "secretAnswer")
  @Size(max = 50, message = "{trainingLevel.answer.Size.message}")
  private String answer;

  /** A blank submission is converted to a null answer variable name when the entity is built */
  @Schema(
      description = "Name to look each participant's own answer up under; blank stores none.",
      example = "username")
  @Size(max = 50, message = "{trainingLevel.answerVariableName.Size.message}")
  private String answerVariableName;

  @Schema(
      description = "The task presented to the participant.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "Play me")
  @NotEmpty(message = "{trainingLevel.content.NotEmpty.message}")
  private String content;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "This is how you do it")
  @NotEmpty(message = "{trainingLevel.solution.NotEmpty.message}")
  private String solution;

  /** Whether requesting the solution reduces the score awardable for the level to zero */
  @Schema(
      description = "Whether showing the solution drops the level's score to zero.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "false")
  @NotNull(message = "{trainingLevel.solutionPenalized.NotNull.message}")
  private boolean solutionPenalized;

  @Schema(description = "Estimated time to solve the level, in minutes.", example = "20")
  private int estimatedDuration;

  /**
   * Number of incorrect answer submissions allowed for the level, against which the number of
   * remaining attempts is calculated
   */
  @Schema(
      description = "How many wrong answers may be submitted before attempts run out.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "5")
  @NotNull(message = "{trainingLevel.incorrectAnswerLimit.NotNull.message}")
  @Min(value = 0, message = "{trainingLevel.incorrectAnswerLimit.Min.message}")
  @Max(value = 100, message = "{trainingLevel.incorrectAnswerLimit.Max.message}")
  private int incorrectAnswerLimit;

  @Valid private Set<HintDTO> hints = new HashSet<>();

  /**
   * Whether each trainee's answer is resolved from the external answer storage service, keyed by
   * the answer variable name, instead of taken from the literal answer field
   */
  @Schema(
      description = "Whether each participant gets their own answer instead of the fixed one.",
      example = "false")
  private boolean variantAnswers;

  @Valid private List<MitreTechniqueDTO> mitreTechniques;

  private Set<String> expectedCommands;

  /**
   * Minimum time, in minutes, a trainee is expected to take on the level; the run's cheat-detection
   * check compares it, converted to seconds, against the elapsed submission time
   */
  @Schema(
      description = "Time in minutes below which a solve is flagged as suspiciously fast.",
      example = "5")
  protected Integer minimalPossibleSolveTime;

  /**
   * Whether the run's cheat-detection check requires at least one submitted command for the level.
   * A request that omits this field deserializes it as false, clearing the entity's default of true
   * rather than leaving it at true.
   */
  @Schema(
      description = "Whether cheating detection expects a command; omitting it stores false.",
      example = "true")
  private boolean commandsRequired;

  /** Sets the level type discriminator to training level */
  public TrainingLevelUpdateDTO() {
    this.levelType = LevelType.TRAINING_LEVEL;
  }
}
