package cz.cyberrange.platform.training.api.dto.hint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * A hint as it appears in a training level being viewed rather than played: its name, its position
 * and what taking it would cost, with the advice itself withheld.
 */
@Data
@ApiModel(
    value = "HintForTrainingLevelViewDTO",
    description = "Basic information about hint viewed in a training level.")
public class HintForTrainingLevelViewDTO {

  @ApiModelProperty(value = "Main identifier of hint.", example = "1")
  private Long id;

  @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
  private String title;

  @ApiModelProperty(
      value = "The number of points the participant loses after receiving the hint.",
      example = "10")
  private Integer hintPenalty;

  @ApiModelProperty(value = "The order of hint in training level", example = "1")
  private int order;
}
