package cz.cyberrange.platform.training.api.dto.assessmentlevel.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * A statement of an extended matching question as previewed to a participant, together with the
 * option order the participant has paired it with so far
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "One statement of a matching question with the option the participant chose.")
public class ExtendedMatchingStatementPreviewDTO implements Ordered {

  @Schema(example = "1")
  private Long id;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "SSH")
  @NotEmpty(message = "{emiStatement.text.NotEmpty.message}")
  private String text;

  /**
   * Position of the statement within its question's list of statements; the participant's own
   * answer for this statement is attributed by indexing that list at this value
   */
  @Schema(description = "Zero-based position of the statement in its question.", example = "0")
  @Min(value = 0, message = "{emiStatement.order.Min.message}")
  private int order;

  /**
   * Order, within the question's extended matching options, of the option the participant selected
   * for this statement; null until they have paired this particular statement. Answering the
   * question does not imply every statement was paired.
   */
  @Schema(
      description = "Order of the option the participant paired with this statement.",
      example = "0")
  private Integer userOptionOrder;
}
