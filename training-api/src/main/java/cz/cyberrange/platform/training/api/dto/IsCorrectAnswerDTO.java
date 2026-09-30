package cz.cyberrange.platform.training.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Response to attempt of answer input, built by hand in {@code TrainingRunFacade} for one submitted
 * answer
 */
@Data
@Schema(description = "The verdict on a submitted answer and what the trainee has left.")
public class IsCorrectAnswerDTO {

  @Schema(example = "false")
  private boolean isCorrect;

  @Schema(description = "How many wrong answers may still be submitted.", example = "3")
  private int remainingAttempts;

  /** Set only once {@code remainingAttempts} has reached zero; left unset otherwise */
  @Schema(
      description = "The level's solution, sent once no attempts remain.",
      example = "This is how you do it")
  private String solution;
}
