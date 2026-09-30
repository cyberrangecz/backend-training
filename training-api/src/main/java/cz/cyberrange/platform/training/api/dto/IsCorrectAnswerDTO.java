package cz.cyberrange.platform.training.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Response to attempt of answer input, built by hand in {@code TrainingRunFacade} for one submitted
 * answer
 */
@Data
@ApiModel(
    value = "IsCorrectAnswerDTO",
    description =
        "A response for the request about the validation of the answer. May also "
            + "include solution if remaining attempts reach 0.")
public class IsCorrectAnswerDTO {

  @ApiModelProperty(value = "True/false if answer has been correct/incorrect.", example = "false")
  private boolean isCorrect;

  @ApiModelProperty(value = "Number of attempts to submit a bad answer.", example = "3")
  private int remainingAttempts;

  /** Set only once {@code remainingAttempts} has reached zero; left unset otherwise */
  @ApiModelProperty(
      value = "Instruction how to get answer in training.",
      example = "This is how you do it")
  private String solution;
}
