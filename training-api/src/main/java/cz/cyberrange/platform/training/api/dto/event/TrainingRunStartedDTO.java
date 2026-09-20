package cz.cyberrange.platform.training.api.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Training run started event, carrying the {@code type} value {@code training_run_started} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Records that a trainee started a training run.")
public class TrainingRunStartedDTO extends TrainingEventDTO {}
