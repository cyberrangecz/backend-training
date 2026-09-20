package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * A statement of an extended matching question, together with the option order that answers it
 * correctly
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "One statement of an extended matching question and its correct option.")
public class ExtendedMatchingStatementDTO implements Ordered {

  @Schema(example = "1")
  private Long id;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "SSH")
  @NotEmpty(message = "{emiStatement.text.NotEmpty.message}")
  private String text;

  /**
   * Position of the statement within its question's list of statements, zero-based and expected
   * contiguous; when a level update resolves its correct option, the statement being completed is
   * looked up by indexing that list at this value
   */
  @Schema(
      description = "Zero-based position of the statement in its question, with no gaps allowed.",
      example = "0")
  @Min(value = 0, message = "{emiStatement.order.Min.message}")
  private int order;

  /**
   * Order, within the question's extended matching options, of the option that correctly answers
   * this statement. It is resolved into the entity's extended matching option relation on every
   * level update, whatever the assessment type, so a statement left without one fails the update
   * rather than being treated as optional; only the check that demands one is confined to a level
   * of the test kind. Cleared to null before an assessment level reaches a trainee's current level
   * in a training run, but left set when a designer retrieves the level for editing.
   */
  @Schema(
      description =
          "Order of the option that answers the statement; required when the assessment is scored as a test.",
      example = "0")
  @Min(value = 0, message = "{emiStatement.correctOptionOrder.Min.message}")
  private Integer correctOptionOrder;
}
