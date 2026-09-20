package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A finding that a level was answered correctly without any console command being recorded for it
 * and without its solution having been revealed. It adds no evidence of its own beyond what every
 * finding carries.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(
    description =
        "A finding that a level was answered correctly with no command run and no solution shown.")
public class NoCommandsDetectionEventDTO extends AbstractDetectionEventDTO {}
