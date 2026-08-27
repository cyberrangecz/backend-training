package cz.cyberrange.platform.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.enums.TRState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Data;

/** Encapsulates information about Training Run */
@Data
@ApiModel(
    value = "TrainingRunByIdDTO",
    description =
        "The act, or a recording, of performing actions during training from a perspective of one concrete participant.")
public class TrainingRunByIdDTO {

  @ApiModelProperty(value = "Main identifier of training run.", example = "1")
  private Long id;

  @ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime startTime;

  @ApiModelProperty(value = "Date when training run ends.", example = "2022-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime endTime;

  /** Copied from the persisted run's own field, which no code in this service ever sets */
  private String eventLogReference;

  @ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
  private TRState state;

  /** Plain sandbox UUID, never hashed; the caller is always this run's trainee or an admin */
  @ApiModelProperty(value = "Reference to the received sandbox.")
  private String sandboxInstanceRefId;

  @ApiModelProperty(value = "Reference to participant of training run.")
  private UserRefDTO participantRef;

  /**
   * Set by {@code TrainingRunFacade}; left unmapped by the mapper, whose source has no matching
   * flat property
   */
  @ApiModelProperty(value = "Id of associated training definition")
  private Long definitionId;

  /**
   * Set by {@code TrainingRunFacade}; left unmapped by the mapper, whose source has no matching
   * flat property
   */
  @ApiModelProperty(value = "Id of associated training instance")
  private Long instanceId;

  /**
   * UUID of the sandbox the run held before it was archived and its sandbox reference cleared;
   * {@code null} while the run has never been archived
   */
  @ApiModelProperty(
      value = "Id of a previous sandbox instance assigned to the training run.",
      example = "12")
  private String previousSandboxInstanceRefId;
}
