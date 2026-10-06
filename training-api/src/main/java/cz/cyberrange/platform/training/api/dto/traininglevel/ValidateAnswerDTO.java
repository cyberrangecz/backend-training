package cz.cyberrange.platform.training.api.dto.traininglevel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * An answer submitted by a trainee for a training level, compared against the level's literal
 * answer or, when the level's answer is variant, against a value resolved per trainee from the
 * external answer storage service
 */
@Data
@Schema(description = "An answer a trainee submits for a training level, to be checked.")
public class ValidateAnswerDTO {
  /** The submitted string, compared case-sensitively against the resolved correct answer */
  @Schema(
      description = "The submitted answer, compared case-sensitively.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "answer")
  @NotEmpty(message = "{answerToValidate.answer.NotEmpty.message}")
  private String answer;
}
