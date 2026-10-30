package cz.cyberrange.platform.training.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** Reports the correct answers of one question of an assessment level */
@Data
@Schema(description = "The correct answers of one question of an assessment level.")
public class QuestionCorrectAnswerDTO {

  @Schema(example = "1")
  private Long questionId;

  @Schema(example = "Which port does SSH listen on by default?")
  private String questionText;

  /**
   * For a multiple choice question its correct choices, for a free form question every accepted
   * answer, and for an extended matching question one {@code statement -> option} pair per
   * statement
   */
  @Schema(
      description =
          "The correct choices of a multiple choice question, every accepted answer of a free form"
              + " question, or one 'statement -> option' pair per statement of an extended"
              + " matching question.",
      example = "[\"22\"]")
  private List<String> correctAnswers = new ArrayList<>();
}
