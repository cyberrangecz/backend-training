package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A finding that a trainee ran commands the detection was configured to forbid. Unlike the other
 * kinds, it implicates exactly one trainee, so the inherited participant string carries a single
 * name.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "ForbiddenCommandsDetectionEventDTO",
    description = "A detection event of type Forbidden Commands.",
    parent = AbstractDetectionEventDTO.class)
public class ForbiddenCommandsDetectionEventDTO extends AbstractDetectionEventDTO {

  /** How many forbidden commands this finding gathered, counting every occurrence */
  @ApiModelProperty(value = "count of forbidden commands.", example = "10")
  private int commandCount;
}
