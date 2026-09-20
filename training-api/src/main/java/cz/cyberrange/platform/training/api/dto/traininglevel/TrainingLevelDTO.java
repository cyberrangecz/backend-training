package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "A training level as authored, including its answer and solution.")
public class TrainingLevelDTO extends AbstractLevelDTO {

  /**
   * The literal keyword a submission is compared against when the level's answer is not variant;
   * ignored in favor of a value resolved per trainee from the external answer storage service
   * otherwise
   */
  @Schema(
      description = "The keyword a submission must match; ignored when answers vary.",
      example = "secretAnswer")
  private String answer;

  /**
   * Key used to look up the answer's value in the external answer storage service when the level's
   * answer is variant
   */
  @Schema(
      description = "Name the participant's own answer is looked up under, when answers vary.",
      example = "username")
  private String answerVariableName;

  /** The task description presented to the participant while attempting the level */
  @Schema(description = "The task presented to the participant.", example = "Play me")
  private String content;

  /**
   * The solution text as authored for the level, carried here without regard to whether any trainee
   * has actually displayed it
   */
  @Schema(
      description = "The solution as authored, whether or not anyone has shown it.",
      example = "This is how you do it")
  private String solution;

  /** Whether requesting the solution reduces the score awardable for the level to zero */
  @Schema(
      description = "Whether showing the solution drops the level's score to zero.",
      example = "true")
  private boolean solutionPenalized;

  private Set<HintDTO> hints = new HashSet<>();

  /**
   * Number of incorrect answer submissions allowed for the level, against which the number of
   * remaining attempts is calculated
   */
  @Schema(
      description = "How many wrong answers may be submitted before attempts run out.",
      example = "5")
  private int incorrectAnswerLimit;

  /**
   * Whether each trainee's answer is resolved from the external answer storage service, keyed by
   * the answer variable name, instead of taken from the literal answer field
   */
  @Schema(
      description = "Whether each participant gets their own answer instead of the fixed one.",
      example = "false")
  private boolean variantAnswers;

  private List<MitreTechniqueDTO> mitreTechniques;

  private Set<String> expectedCommands;

  /** Whether the run's cheat-detection check requires at least one submitted command */
  @Schema(
      description = "Whether cheating detection expects at least one command from the run.",
      example = "true")
  private boolean commandsRequired;

  /**
   * Minimum time, in minutes, a trainee is expected to take on the level; the run's cheat-detection
   * check compares it, converted to seconds, against the elapsed submission time
   */
  @Schema(
      description = "Time in minutes below which a solve is flagged as suspiciously fast.",
      example = "5")
  private Integer minimalPossibleSolveTime;
}
