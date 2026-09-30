package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "A finding that a trainee ran commands the detection was told to forbid.")
public class ForbiddenCommandsDetectionEventDTO extends AbstractDetectionEventDTO {

  /** How many forbidden commands this finding gathered, counting every occurrence */
  @Schema(
      example = "10",
      description = "How many forbidden commands the finding gathered, counting repeats.")
  private int commandCount;
}
