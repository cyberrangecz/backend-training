package cz.cyberrange.platform.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
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

/** Encapsulates information about training level. Inherits from {@link AbstractLevelImportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "A training level to create, the task a trainee solves")
@JsonIgnoreProperties({"reference_solution", "order"})
public class TrainingLevelImportDTO extends AbstractLevelImportDTO {

  /**
   * Blank is normalized to {@code null} on import. Required, and {@link #answerVariableName} must
   * then be {@code null}, when {@link #variantAnswers} is {@code false}; forbidden otherwise.
   */
  @Schema(
      description = "The correct answer, required when every trainee is given the same one",
      example = "secretAnswer")
  @Size(max = 50, message = "{trainingLevel.answer.Size.message}")
  @JsonAlias({"flag"})
  private String answer;

  /**
   * Blank is normalized to {@code null} on import. Required, and {@link #answer} must then be
   * {@code null}, when {@link #variantAnswers} is {@code true}; forbidden otherwise.
   */
  @Schema(
      description =
          "Identifier each trainee's own answer is fetched under, required when answers vary",
      example = "username")
  private String answerVariableName;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Play me")
  @NotEmpty(message = "{trainingLevel.content.NotEmpty.message}")
  private String content;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "This is how you do it")
  @NotEmpty(message = "{trainingLevel.solution.NotEmpty.message}")
  private String solution;

  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Whether displaying the solution costs the trainee the level's whole score",
      example = "true")
  @NotNull(message = "{trainingLevel.solutionPenalized.NotNull.message}")
  private Boolean solutionPenalized;

  /** Rejected on import if the sum of every hint's penalty exceeds {@link #maxScore} */
  @Valid private Set<HintImportDTO> hints = new HashSet<>();

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
  @NotNull(message = "{trainingLevel.incorrectAnswerLimit.NotNull.message}")
  @Min(value = 0, message = "{trainingLevel.incorrectAnswerLimit.Min.message}")
  @Max(value = 100, message = "{trainingLevel.incorrectAnswerLimit.Max.message}")
  @JsonAlias({"incorrect_flag_limit"})
  private Integer incorrectAnswerLimit;

  @Valid
  @Schema(example = "[]")
  private List<AttachmentImportDTO> attachments;

  /** Caps the total penalty the level's {@link #hints} may carry; see {@link #hints} */
  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Points for solving the level, before any hint or solution penalty",
      example = "20")
  @NotNull(message = "{abstractLevel.maxScore.NotNull.message}")
  @Min(value = 0, message = "{abstractLevel.maxScore.Min.message}")
  @Max(value = 100, message = "{abstractLevel.maxScore.Max.message}")
  protected Integer maxScore;

  /**
   * Selects which of {@link #answer} or {@link #answerVariableName} the import requires: {@code
   * true} requires {@link #answerVariableName}, {@code false} requires {@link #answer}
   */
  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Whether each trainee is given their own correct answer",
      example = "false")
  @NotNull(message = "{trainingLevel.variantAnswers.NotNull.message}")
  private Boolean variantAnswers;

  @Valid private List<MitreTechniqueDTO> mitreTechniques;

  private Set<String> expectedCommands;

  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Whether a correct answer with no command run beforehand counts as cheating",
      example = "true")
  @NotNull(message = "{trainingLevel.commandsRequired.NotNull.message}")
  private Boolean commandsRequired;
}
