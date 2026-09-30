package cz.cyberrange.platform.training.api.dto.run;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about Training Run */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "A training run with the state of its logging and its cheating detections")
public class TrainingRunDTO extends TrainingRunBasicDTO {

  /** Copied from the persisted run's own field, which no code in this service ever sets */
  private String eventLogReference;

  private Integer sandboxInstanceAllocationId;

  /** False unless the caller explicitly resolves and sets this run's logging state */
  @Schema(example = "true")
  private boolean eventLoggingState;

  /** False unless the caller explicitly resolves and sets this run's logging state */
  @Schema(example = "true")
  private boolean commandLoggingState;

  @Schema(example = "true")
  private boolean hasDetectionEvent;
}
