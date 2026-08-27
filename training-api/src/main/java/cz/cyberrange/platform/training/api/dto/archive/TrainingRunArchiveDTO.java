package cz.cyberrange.platform.training.api.dto.archive;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.enums.TRState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Snapshot of one participant's training run, written as a single JSON file into the training runs
 * folder of the training instance archive.
 */
@Data
@ApiModel(
    value = "TrainingRunArchiveDTO",
    description = "An archived run of training instance of a particular participant.")
public class TrainingRunArchiveDTO {

  @ApiModelProperty(value = "Main identifier of training run.", example = "1")
  private Long id;

  /**
   * Primary key of the training instance this run belongs to. The training run entity carries no
   * matching property, so the archiving facade sets this field itself after mapping the run.
   */
  @ApiModelProperty(
      value = "Main identifier of training instance associated with this run.",
      example = "1")
  private Long instanceId;

  @ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime startTime;

  @ApiModelProperty(value = "Date when training run ends.", example = "2022-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime endTime;

  private String eventLogReference;

  @ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
  private TRState state;

  /**
   * User reference id ({@code UserRef.userRefId}) of the run's participant, not the participant's
   * local primary key.
   */
  @ApiModelProperty(value = "Reference to participant of training run.", example = "5")
  private Long participantRefId;
}
