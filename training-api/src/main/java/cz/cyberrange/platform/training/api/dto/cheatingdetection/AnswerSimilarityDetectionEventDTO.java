package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A finding that a trainee submitted an answer generated for somebody else, or for a different
 * level than the one being answered.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "AnswerSimilarityDetectionEventDTO",
    description = "A detection event of type Answer Similarity.",
    parent = AbstractDetectionEventDTO.class)
public class AnswerSimilarityDetectionEventDTO extends AbstractDetectionEventDTO {
  /** The answer value that was submitted, as the trainee typed it. */
  @ApiModelProperty(value = "Correct answer to the level.", example = "pass")
  private String answer;

  /** Display name of the trainee the submitted answer was generated for. */
  @ApiModelProperty(
      value = "Name of a player who was assigned the correct answer.",
      example = "John Doe")
  private String answerOwner;
}
