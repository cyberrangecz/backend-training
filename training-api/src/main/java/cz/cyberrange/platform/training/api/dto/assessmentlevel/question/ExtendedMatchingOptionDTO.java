package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/** An option of an extended matching question, identified within the question by its position */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "One option offered by an extended matching question.")
public class ExtendedMatchingOptionDTO implements Ordered {

  @Schema(example = "1")
  private Long id;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "22")
  @NotEmpty(message = "{emiOption.text.NotEmpty.message}")
  private String text;

  /**
   * Position of the option within its question's list of extended matching options, zero-based and
   * expected contiguous; a statement's correct option is looked up by indexing that list at its own
   * {@code correctOptionOrder} value
   */
  @Schema(
      description = "Zero-based position of the option in its question, with no gaps allowed.",
      example = "0")
  @Min(value = 0, message = "{emiOption.order.Min.message}")
  private int order;
}
