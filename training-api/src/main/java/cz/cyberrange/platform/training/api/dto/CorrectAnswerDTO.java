package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Reports one training level's identity and correct answer, built by hand in {@code
 * TrainingRunFacade} from a {@code TrainingLevel} entity and returned in bulk from the training
 * run's answers endpoint. A level with no variant answers leaves {@code variableName} unset, which
 * the class-level {@code @JsonInclude} then omits from the response entirely.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CorrectAnswerDTO {

  @ApiModelProperty(value = "Main identifier of the training level.", example = "1")
  private Long levelId;

  @ApiModelProperty(
      value = "Short textual description of the training level.",
      example = "Training Level1")
  private String levelTitle;

  @ApiModelProperty(value = "Order of level, starts with 0", example = "2")
  private Integer levelOrder;

  /**
   * Holds the level's static answer, or, for a level using variant answers, whatever value the
   * variant-answer lookup resolved for it, which may be null when that lookup found none
   */
  @ApiModelProperty(
      value = "Correct answer (static or variable) of the training level.",
      example = "john")
  private String correctAnswer;

  /**
   * Names the answer variable the level was configured with; unset when the level uses a single
   * static answer
   */
  @ApiModelProperty(value = "Identifier of the variant answer.", example = "username")
  private String variableName;
}
