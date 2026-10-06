package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;

/**
 * A choice offered for a multiple-choice or free-form question, with its position and whether it is
 * a correct answer
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "One answer choice of a question, with its position and correctness.")
public class QuestionChoiceDTO implements Serializable, Ordered {

  @Schema(example = "1")
  private Long id;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Yes")
  @NotEmpty(message = "{questionChoice.text.NotEmpty.message}")
  private String text;

  /**
   * Whether this choice is a correct answer. Consulted only when scoring a multiple-choice
   * question, where every submitted answer must equal the text of exactly the choices marked
   * correct; ignored for a free-form question, whose choices are all treated as accepted answer
   * texts regardless of this flag. Cleared to null before an assessment level reaches a trainee's
   * current level in a training run, but left set when a designer retrieves the level for editing.
   */
  @Schema(
      description = "Whether this choice is a correct answer; withheld while playing the level.",
      example = "true")
  @NotNull(message = "{questionChoice.correct.NotNull.message}")
  private Boolean correct;

  @Schema(
      description = "Zero-based position of the choice in its question, with no gaps allowed.",
      example = "1")
  @Min(value = 0, message = "{questionChoice.order.Min.message}")
  private int order;
}
