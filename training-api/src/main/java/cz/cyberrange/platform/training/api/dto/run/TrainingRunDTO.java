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

  /**
   * UUID of the sandbox the run held before it was archived and its sandbox reference cleared;
   * {@code null} while the run has never been archived
   */
  @Schema(
      description = "UUID of the sandbox the run held before it was archived.",
      example = "d2f6b1c4-9a3e-4c07-8b52-1e7a5c9d3f80")
  private String previousSandboxInstanceRefId;

  /** False unless the caller explicitly resolves and sets this run's logging state */
  @Schema(example = "true")
  private boolean eventLoggingState;

  /** False unless the caller explicitly resolves and sets this run's logging state */
  @Schema(example = "true")
  private boolean commandLoggingState;

  @Schema(example = "true")
  private boolean hasDetectionEvent;
}
