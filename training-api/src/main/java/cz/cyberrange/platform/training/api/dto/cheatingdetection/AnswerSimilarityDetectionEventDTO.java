package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A finding that a trainee submitted an answer generated for somebody else, or for a different
 * level than the one being answered
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(
    description =
        "A finding that a submitted answer was generated for another trainee or another level.")
public class AnswerSimilarityDetectionEventDTO extends AbstractDetectionEventDTO {
  /** The answer value that was submitted, as the trainee typed it */
  @Schema(example = "pass")
  private String answer;

  /** Display name of the trainee the submitted answer was generated for */
  @Schema(
      example = "John Doe",
      description = "Name of the trainee the submitted answer was generated for.")
  private String answerOwner;
}
