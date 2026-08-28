package cz.cyberrange.platform.training.api.dto.traininglevel;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * An answer submitted by a trainee for a training level, compared against the level's literal
 * answer or, when the level's answer is variant, against a value resolved per trainee from the
 * external answer storage service
 */
@Data
public class ValidateAnswerDTO {
  /** The submitted string, compared case-sensitively against the resolved correct answer */
  @ApiModelProperty(value = "Answer to be validated.", required = true, example = "answer")
  @NotEmpty(message = "{answerToValidate.answer.NotEmpty.message}")
  private String answer;
}
