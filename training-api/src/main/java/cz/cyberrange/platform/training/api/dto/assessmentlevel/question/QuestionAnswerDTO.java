package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * A participant's submitted answer to one question of an assessment level, in whichever of {@code
 * answers} or {@code extendedMatchingPairs} applies to the question's type
 */
@Data
@ApiModel(value = "QuestionAnswerDTO")
public class QuestionAnswerDTO {

  @ApiModelProperty(value = "ID of answered question", example = "1")
  @NotNull(message = "{questionAnswer.questionId.NotNull.message}")
  private Long questionId;

  /**
   * The participant's submitted answer for a free-form question, or the text of every choice the
   * participant selected for a multiple-choice question; unused for an extended matching question.
   */
  @ApiModelProperty(value = "Answers to the question", example = "[\"An answer\"]")
  private Set<String> answers;

  /**
   * For an extended matching question, each entry maps a statement's order to the order of the
   * option the participant paired it with; unused for a free-form or multiple-choice question.
   */
  @ApiModelProperty(
      value = "Mapping of the answers to question of type extended matching items",
      example = "{ \"1\": [2, 3]")
  private Map<Integer, Integer> extendedMatchingPairs;
}
