package cz.cyberrange.platform.training.api.dto.assessmentlevel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Assessment level content submitted to replace the current content of an assessment level. Its
 * maximal score is always recomputed from the questions' point values, regardless of any score
 * value present in the request.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "New questions, instructions and type to store on an assessment level.")
@JsonIgnoreProperties(value = {"max_score"})
public class AssessmentLevelUpdateDTO extends AbstractLevelUpdateDTO {

  /**
   * Questions of the level, replacing its current set. When the level is of assessment type TEST,
   * every extended matching statement in every question must carry a correct option order, or the
   * update is rejected.
   */
  @Valid private List<QuestionDTO> questions = new ArrayList<>();

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Fill me up slowly")
  @NotNull(message = "{assessmentLevel.instructions.NotNull.message}")
  private String instructions;

  /**
   * The assessment's type, copied onto the entity's assessment type. {@code TEST} requires every
   * EMI question's statements to carry a correct option order and causes answering the level to
   * score each question by its points and penalty; any other type only records the participant's
   * responses without scoring them.
   */
  @Schema(
      description = "Whether the answers are graded (TEST) or only collected (QUESTIONNAIRE).",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "TEST")
  @NotNull(message = "{assessmentLevel.type.NotNull.message}")
  private AssessmentType type;

  /**
   * Estimated time, in minutes, to solve the level. Added into the owning training definition's
   * total estimated duration in place of the level's previous contribution.
   */
  @Schema(
      description = "Estimated time to solve the level, in minutes.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "5")
  private int estimatedDuration;

  /**
   * Threshold, in minutes, below which a participant's time spent on the level is flagged as an
   * unusually fast solve by cheating detection
   */
  @Schema(
      description = "Time in minutes below which a solve is flagged as suspiciously fast.",
      example = "5")
  protected Integer minimalPossibleSolveTime;

  /** Sets the level type discriminator to assessment level */
  public AssessmentLevelUpdateDTO() {
    this.levelType = LevelType.ASSESSMENT_LEVEL;
  }
}
