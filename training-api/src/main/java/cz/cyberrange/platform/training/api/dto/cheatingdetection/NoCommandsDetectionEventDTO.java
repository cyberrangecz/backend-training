package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
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
@ApiModel(
    value = "NoCommandsDetectionEventDTO",
    description = "A detection event of type No Commands.",
    parent = AbstractDetectionEventDTO.class)
public class NoCommandsDetectionEventDTO extends AbstractDetectionEventDTO {}
