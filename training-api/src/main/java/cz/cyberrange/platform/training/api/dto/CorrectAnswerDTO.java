package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Reports one training level's identity and correct answer, built by hand in {@code
 * TrainingRunFacade} from a {@code TrainingLevel} entity and returned in bulk from the training
 * run's answers endpoint. A level with no variant answers leaves {@code variableName} unset, which
 * the class-level {@code @JsonInclude} then omits from the response entirely.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "One training level's correct answer, resolved for the asking participant.")
public class CorrectAnswerDTO {

  @Schema(example = "1")
  private Long levelId;

  @Schema(example = "Training Level1")
  private String levelTitle;

  @Schema(description = "Zero-based position of the level within its definition.", example = "2")
  private Integer levelOrder;

  /**
   * Holds the level's static answer, or, for a level using variant answers, whatever value the
   * variant-answer lookup resolved for it, which may be null when that lookup found none
   */
  @Schema(
      description = "The level's answer, resolved for this participant when answers vary.",
      example = "john")
  private String correctAnswer;

  /**
   * Names the answer variable the level was configured with; unset when the level uses a single
   * static answer
   */
  @Schema(
      description = "The answer variable the level uses; absent for a fixed answer.",
      example = "username")
  private String variableName;
}
