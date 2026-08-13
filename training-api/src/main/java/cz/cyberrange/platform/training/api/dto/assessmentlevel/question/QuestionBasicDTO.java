package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonInclude
@ApiModel(value = "QuestionBasicDTO", description = "Question in an assessment level.")
public class QuestionBasicDTO {

  @ApiModelProperty(value = "Main identifier of the question.", example = "1")
  protected Long id;

  @ApiModelProperty(value = "Type of the question.", required = true, example = "FFQ")
  @NotNull(message = "{question.questionType.NotNull.message}")
  protected QuestionType questionType;

  @ApiModelProperty(
      value =
          "Number of points the participant will receive for the correct answering of the question.",
      example = "10")
  @Min(value = 0, message = "{question.points.Min.message}")
  protected int points;

  @ApiModelProperty(
      value =
          "Number of points the participant will lose for the incorrect answering of the question.",
      example = "6")
  @Min(value = 0, message = "{question.penalty.Min.message}")
  protected int penalty;

  @ApiModelProperty(value = "Order of the question, starts with 0", example = "0")
  @Min(value = 0, message = "{question.order.Min.message}")
  protected int order;

  @ApiModelProperty(
      value = "Sign if the question must be answered by the participant or not.",
      example = "true")
  @NotNull(message = "{question.answerRequired.NotNull.message}")
  protected boolean answerRequired;
}
