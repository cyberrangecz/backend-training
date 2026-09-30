package cz.cyberrange.platform.training.api.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Training run resumed event, carrying the {@code type} value {@code training_run_resumed} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Records that a trainee returned to a training run already under way.")
public class TrainingRunResumedDTO extends TrainingEventDTO {}
