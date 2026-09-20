package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A finding that several trainees solved the same level within moments of each other, closer
 * together than the detection's configured tolerance
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "A finding that several trainees solved the same level moments apart.")
public class TimeProximityDetectionEventDTO extends AbstractDetectionEventDTO {

  /** The tolerance the detection was run with, copied onto the finding as it was made */
  @Schema(example = "1", description = "The closeness the run was configured with, in seconds.")
  private Long threshold;
}
