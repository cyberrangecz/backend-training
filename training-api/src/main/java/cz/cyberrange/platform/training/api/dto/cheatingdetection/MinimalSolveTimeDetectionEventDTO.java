package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A finding that a level was solved faster than it is believed to be solvable, gathering everyone
 * who beat that time on it
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "MinimalSolveTimeDetectionEventDTO",
    description = "A detection event of type Minimal Solve Time.",
    parent = AbstractDetectionEventDTO.class)
public class MinimalSolveTimeDetectionEventDTO extends AbstractDetectionEventDTO {

  /**
   * The time the level is configured as needing at the very least, in seconds, converted from the
   * minutes it is configured in
   */
  @ApiModelProperty(value = "Minimal time required to solve the level.", example = "1")
  private Long minimalSolveTime;
}
