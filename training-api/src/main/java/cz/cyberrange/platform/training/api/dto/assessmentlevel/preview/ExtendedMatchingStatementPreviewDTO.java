package cz.cyberrange.platform.training.api.dto.assessmentlevel.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * A statement of an extended matching question as previewed to a participant, together with the
 * option order the participant has paired it with so far
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedMatchingStatementPreviewDTO implements Ordered {

  @ApiModelProperty(value = "Main identifier of the extended matching statement.", example = "1")
  private Long id;

  @ApiModelProperty(value = "Text content of the extended matching statement.", example = "SSH")
  @NotEmpty(message = "{emiStatement.text.NotEmpty.message}")
  private String text;

  /**
   * Position of the statement within its question's list of statements; the participant's own
   * answer for this statement is attributed by indexing that list at this value
   */
  @ApiModelProperty(value = "The order of the statement in question of type EMI.", example = "0")
  @Min(value = 0, message = "{emiStatement.order.Min.message}")
  private int order;

  /**
   * Order, within the question's extended matching options, of the option the participant selected
   * for this statement; null until they have paired this particular statement. Answering the
   * question does not imply every statement was paired.
   */
  @ApiModelProperty(
      value = "The order of the user chosen option from the list of extended matching options.",
      example = "0")
  private Integer userOptionOrder;
}
