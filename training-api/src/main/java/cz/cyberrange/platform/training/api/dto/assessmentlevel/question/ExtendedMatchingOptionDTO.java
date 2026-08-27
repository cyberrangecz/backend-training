package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedMatchingOptionDTO implements Ordered {

  @ApiModelProperty(value = "Main identifier of the extended matching option.", example = "1")
  private Long id;

  @ApiModelProperty(value = "Text content of the extended matching option.", example = "22")
  @NotEmpty(message = "{emiOption.text.NotEmpty.message}")
  private String text;

  /**
   * Position of the option within its question's list of extended matching options, zero-based and
   * expected contiguous; a statement's correct option is looked up by indexing that list at its own
   * {@code correctOptionOrder} value.
   */
  @ApiModelProperty(value = "The order of the option in question of type EMI.", example = "0")
  @Min(value = 0, message = "{emiOption.order.Min.message}")
  private int order;
}
