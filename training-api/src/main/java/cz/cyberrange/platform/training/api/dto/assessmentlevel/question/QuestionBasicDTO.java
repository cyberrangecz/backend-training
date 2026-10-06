package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * A question of an assessment level, carrying its type, scoring, position, and whether an answer is
 * required
 */
@Data
@JsonInclude
@Schema(description = "An assessment question without its text or answer options.")
public class QuestionBasicDTO {

  @Schema(example = "1")
  protected Long id;

  @Schema(
      description = "Free-form (FFQ), multiple choice (MCQ) or extended matching (EMI).",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "FFQ")
  @NotNull(message = "{question.questionType.NotNull.message}")
  protected QuestionType questionType;

  /**
   * Points added to the participant's assessment score when this question is answered correctly in
   * a TEST-type assessment; also contributes to the owning level's maximal score, which is
   * recomputed from every question's points whenever the level is saved
   */
  @Schema(
      description = "Points a correct answer adds to the score in a graded (TEST) level.",
      example = "10")
  @Min(value = 0, message = "{question.points.Min.message}")
  protected int points;

  /**
   * Points subtracted from the participant's assessment score when this question is answered
   * incorrectly in a TEST-type assessment; has no effect on any other assessment type, whose
   * responses are recorded without being scored
   */
  @Schema(
      description = "Points a wrong answer takes off the score in a graded (TEST) level.",
      example = "6")
  @Min(value = 0, message = "{question.penalty.Min.message}")
  protected int penalty;

  /**
   * Position of the question within its assessment level, counted from zero. When a TEST-type level
   * update resolves a question's correct extended matching options, the question is looked up by
   * indexing the level's saved question list at this value. Nothing checks that the values across a
   * level's questions are contiguous or within range, so a value past the end fails at that lookup.
   */
  @Schema(description = "Zero-based position of the question in its level.", example = "0")
  @Min(value = 0, message = "{question.order.Min.message}")
  protected int order;

  /**
   * Whether the participant must answer this question. Enforced only for a non-TEST assessment,
   * where submitting a response without one for a required question is rejected; a TEST-type
   * assessment already requires every one of its questions to be answered.
   */
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
  @NotNull(message = "{question.answerRequired.NotNull.message}")
  protected boolean answerRequired;
}
