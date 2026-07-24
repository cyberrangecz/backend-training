package cz.cyberrange.platform.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.enums.TRState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Contains generally safe, descriptive-only data accessible by both organizers and trainees. */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(
    value = "TrainingRunBasicDTO",
    description =
        "The act, or a recording, of performing actions during training from a perspective of one concrete participant.")
public class TrainingRunBasicDTO {
  @ApiModelProperty(value = "Main identifier of training run.", example = "1")
  protected Long id;

  @ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
  protected TRState state;

  @ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  protected LocalDateTime startTime;

  @ApiModelProperty(value = "Date when training run ends.", example = "2022-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  protected LocalDateTime endTime;

  @ApiModelProperty(value = "Reference to participant of training run.")
  protected UserRefDTO participantRef;

  @ApiModelProperty(
      value =
          "Reference to the received sandbox. Plain sandbox UUID for administrators, organizers of"
              + " the runs, and for the caller's own run; SHA-256 hash for other participants'"
              + " runs.")
  protected String sandboxInstanceRefId;

  @ApiModelProperty(value = "Id of owning instance id", example = "1")
  protected Long trainingInstanceId;

  @ApiModelProperty(
      value = "Id of training definition from which is training run created.",
      example = "1")
  protected Long trainingDefinitionId;

  @ApiModelProperty(value = "Id of the current level in training run.", example = "1")
  protected Long currentLevelId;

  @ApiModelProperty(value = "Order of the current level in training run.", example = "1")
  protected Integer currentLevelOrder;
}
