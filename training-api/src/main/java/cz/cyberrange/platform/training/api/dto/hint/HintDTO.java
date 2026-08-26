package cz.cyberrange.platform.training.api.dto.hint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about Hint. */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "HintDTO", description = "A brief textual description to aid the participant.")
public class HintDTO extends HintBasicDTO {

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Very good advice")
  @NotEmpty(message = "{hint.content.NotEmpty.message}")
  private String content;

  /** Position of the hint within its level's sequence of hints. */
  @ApiModelProperty(value = "The order of hint in training level", example = "1")
  @Min(value = 0, message = "{hint.order.Min.message}")
  private int order;
}
