package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "A finding that a level was solved faster than it is thought to be solvable.")
public class MinimalSolveTimeDetectionEventDTO extends AbstractDetectionEventDTO {

  /**
   * The time the level is configured as needing at the very least, in seconds, converted from the
   * minutes it is configured in
   */
  @Schema(
      example = "1",
      description = "The shortest time the level is expected to need, in seconds.")
  private Long minimalSolveTime;
}
