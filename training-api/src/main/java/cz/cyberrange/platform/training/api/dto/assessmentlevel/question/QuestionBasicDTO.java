package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * A question of an assessment level, carrying its type, scoring, position, and whether an answer is
 * required
 */
@Data
@JsonInclude
@ApiModel(value = "QuestionBasicDTO", description = "Question in an assessment level.")
public class QuestionBasicDTO {

  @ApiModelProperty(value = "Main identifier of the question.", example = "1")
  protected Long id;

  @ApiModelProperty(value = "Type of the question.", required = true, example = "FFQ")
  @NotNull(message = "{question.questionType.NotNull.message}")
  protected QuestionType questionType;

  /**
   * Points added to the participant's assessment score when this question is answered correctly in
   * a TEST-type assessment; also contributes to the owning level's maximal score, which is
   * recomputed from every question's points whenever the level is saved
   */
  @ApiModelProperty(
      value =
          "Number of points the participant will receive for the correct answering of the question.",
      example = "10")
  @Min(value = 0, message = "{question.points.Min.message}")
  protected int points;

  /**
   * Points subtracted from the participant's assessment score when this question is answered
   * incorrectly in a TEST-type assessment; has no effect on any other assessment type, whose
   * responses are recorded without being scored
   */
  @ApiModelProperty(
      value =
          "Number of points the participant will lose for the incorrect answering of the question.",
      example = "6")
  @Min(value = 0, message = "{question.penalty.Min.message}")
  protected int penalty;

  /**
   * Position of the question within its assessment level, counted from zero. When a TEST-type level
   * update resolves a question's correct extended matching options, the question is looked up by
   * indexing the level's saved question list at this value. Nothing checks that the values across a
   * level's questions are contiguous or within range, so a value past the end fails at that lookup.
   */
  @ApiModelProperty(value = "Order of the question, starts with 0", example = "0")
  @Min(value = 0, message = "{question.order.Min.message}")
  protected int order;

  /**
   * Whether the participant must answer this question. Enforced only for a non-TEST assessment,
   * where submitting a response without one for a required question is rejected; a TEST-type
   * assessment already requires every one of its questions to be answered.
   */
  @ApiModelProperty(
      value = "Sign if the question must be answered by the participant or not.",
      example = "true")
  @NotNull(message = "{question.answerRequired.NotNull.message}")
  protected boolean answerRequired;
}
