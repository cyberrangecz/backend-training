package cz.cyberrange.platform.training.api.dto.assessmentlevel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.cyberrange.platform.training.api.dto.AbstractLevelContentDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Holds the questions, instructions and type of an assessment level in a request. The level's
 * maximal score is the sum of its questions' points; a score in the request is ignored. Extended by
 * {@link AssessmentLevelCreateDTO} and {@link AssessmentLevelUpdateDTO}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(value = {"max_score"})
public abstract class AssessmentLevelContentDTO extends AbstractLevelContentDTO
    implements AssessmentQuestionsSource {

  /**
   * Questions of the level in presentation order. In an assessment of type TEST, every extended
   * matching statement names its correct option.
   */
  @Valid private List<QuestionDTO> questions = new ArrayList<>();

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Fill me up slowly")
  @NotNull(message = "{assessmentLevel.instructions.NotNull.message}")
  private String instructions;

  /**
   * The assessment's type. {@code TEST} scores each answered question by its points and penalty and
   * requires every extended matching statement to name its correct option; {@code QUESTIONNAIRE}
   * only records the participant's responses.
   */
  @Schema(
      description = "Whether the answers are graded (TEST) or only collected (QUESTIONNAIRE).",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "TEST")
  @NotNull(message = "{assessmentLevel.type.NotNull.message}")
  private AssessmentType type;

  /**
   * Threshold, in minutes, below which a participant's time spent on the level is flagged as an
   * unusually fast solve by cheating detection
   */
  @Schema(
      description = "Time in minutes below which a solve is flagged as suspiciously fast.",
      example = "5")
  protected Integer minimalPossibleSolveTime;

  /** Sets the level type discriminator to assessment level */
  protected AssessmentLevelContentDTO() {
    this.levelType = LevelType.ASSESSMENT_LEVEL;
  }
}
