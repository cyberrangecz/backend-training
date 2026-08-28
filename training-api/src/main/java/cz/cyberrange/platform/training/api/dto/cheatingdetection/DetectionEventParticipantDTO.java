package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * One trainee implicated in a detection finding, together with the submission of theirs that put
 * them there
 */
@Data
@ApiModel(
    value = "DetectionEventParticipantDTO",
    description = "Basic Information about a detection event participant.")
public class DetectionEventParticipantDTO {

  @ApiModelProperty(value = "Ip address of participant.", example = "1.1.1.1")
  private String ipAddress;

  @ApiModelProperty(value = "Time when the event occurred.", example = "1.1.2022 5:55:23")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime occurredAt;

  /**
   * How long this trainee took over the level, in seconds, carried only by a finding that turns on
   * solving speed and left unset otherwise
   */
  @ApiModelProperty(value = "Time in which the level was solved.", example = "20")
  private Long solvedInTime;

  @ApiModelProperty(value = "Name of the participant.", example = "John Doe")
  private String participantName;

  /**
   * The trainee's {@code userRefId}, the id spoken outside this service, rather than the local
   * primary key of the user row
   */
  @ApiModelProperty(value = "User id of participant.", example = "6")
  private Long userId;

  @ApiModelProperty(value = "the id of detection event", example = "3")
  private Long detectionEventId;
}
