package cz.cyberrange.platform.training.api.dto.hint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Encapsulates information about hint that was already taken */
@Data
@ApiModel(
    value = "TakenHintDTO",
    description = "A taken brief textual description to aid the participant.")
public class TakenHintDTO {

  @ApiModelProperty(value = "Main identifier of hint.", example = "1")
  private Long id;

  @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
  private String title;

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Very good advice")
  private String content;

  @EqualsAndHashCode.Exclude
  @ApiModelProperty(value = "The order of hint in training level", example = "1")
  private int order;
}
