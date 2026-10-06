package cz.cyberrange.platform.training.api.dto.run;

import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.enums.TRState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

/** Encapsulates information about Training Run */
@Data
@Schema(description = "One training run in full, with its participant resolved")
public class TrainingRunByIdDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime startTime;

  @Schema(example = "2022-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime endTime;

  /** Copied from the persisted run's own field, which no code in this service ever sets */
  private String eventLogReference;

  @Schema(example = "ALLOCATED")
  private TRState state;

  /** Plain sandbox UUID, never hashed; the caller is always this run's trainee or an admin */
  private String sandboxInstanceRefId;

  private UserRefDTO participantRef;

  /**
   * Set by {@code TrainingRunFacade}; left unmapped by the mapper, whose source has no matching
   * flat property
   */
  private Long definitionId;

  /**
   * Set by {@code TrainingRunFacade}; left unmapped by the mapper, whose source has no matching
   * flat property
   */
  private Long instanceId;

  /**
   * UUID of the sandbox the run held before it was archived and its sandbox reference cleared;
   * {@code null} while the run has never been archived
   */
  @Schema(
      description = "UUID of the sandbox the run held before it was archived.",
      example = "d2f6b1c4-9a3e-4c07-8b52-1e7a5c9d3f80")
  private String previousSandboxInstanceRefId;
}
