package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;
import lombok.Data;

/**
 * A participant's submitted answer to one question of an assessment level, in whichever of {@code
 * answers} or {@code extendedMatchingPairs} applies to the question's type
 */
@Data
@Schema(description = "One question's answer as submitted by the participant.")
public class QuestionAnswerDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  @NotNull(message = "{questionAnswer.questionId.NotNull.message}")
  private Long questionId;

  /**
   * The participant's submitted answer for a free-form question, or the text of every choice the
   * participant selected for a multiple-choice question; unused for an extended matching question
   */
  @Schema(
      description = "The submitted text, or the text of each chosen option.",
      example = "[\"An answer\"]")
  private Set<String> answers;

  /**
   * For an extended matching question, each entry maps a statement's order to the order of the
   * option the participant paired it with; unused for a free-form or multiple-choice question
   */
  @Schema(
      description = "Each entry pairs a statement's order with the chosen option's order.",
      example = "{\"0\": 1, \"1\": 0}")
  private Map<Integer, Integer> extendedMatchingPairs;
}
