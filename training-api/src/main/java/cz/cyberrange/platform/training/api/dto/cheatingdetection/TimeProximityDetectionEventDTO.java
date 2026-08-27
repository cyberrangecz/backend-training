package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A finding that several trainees solved the same level within moments of each other, closer
 * together than the detection's configured tolerance.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "TimeProximityDetectionEventDTO",
    description = "A detection event of type Time Proximity.",
    parent = AbstractDetectionEventDTO.class)
public class TimeProximityDetectionEventDTO extends AbstractDetectionEventDTO {

  /** The tolerance the detection was run with, copied onto the finding as it was made. */
  @ApiModelProperty(value = "Time threshold for detection.", example = "1")
  private Long threshold;
}
