package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedMatchingStatementDTO implements Ordered {

  @ApiModelProperty(value = "Main identifier of the extended matching statement.", example = "1")
  private Long id;

  @ApiModelProperty(value = "Text content of the extended matching statement.", example = "SSH")
  @NotEmpty(message = "{emiStatement.text.NotEmpty.message}")
  private String text;

  /**
   * Position of the statement within its question's list of statements, zero-based and expected
   * contiguous; when a level update resolves its correct option, the statement being completed is
   * looked up by indexing that list at this value.
   */
  @ApiModelProperty(value = "The order of the statement in question of type EMI.", example = "0")
  @Min(value = 0, message = "{emiStatement.order.Min.message}")
  private int order;

  /**
   * Order, within the question's extended matching options, of the option that correctly answers
   * this statement. It is resolved into the entity's extended matching option relation on every
   * level update, whatever the assessment type, so a statement left without one fails the update
   * rather than being treated as optional; only the check that demands one is confined to a level
   * of the test kind. Cleared to null before an
   * assessment level reaches a trainee's current level in a training run, but left set when a
   * designer retrieves the level for editing.
   */
  @ApiModelProperty(
      value = "The order of the correct option in the list of extended matching options.",
      example = "0")
  @Min(value = 0, message = "{emiStatement.correctOptionOrder.Min.message}")
  private Integer correctOptionOrder;
}
