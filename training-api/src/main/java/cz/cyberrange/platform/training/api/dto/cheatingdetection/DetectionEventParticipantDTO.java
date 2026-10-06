package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * One trainee implicated in a detection finding, together with the submission of theirs that put
 * them there
 */
@Data
@Schema(description = "One trainee implicated in a finding, with the submission that named them.")
public class DetectionEventParticipantDTO {

  @Schema(example = "1.1.1.1", description = "Address the submission came from.")
  private String ipAddress;

  @Schema(example = "2022-01-01T05:55:23Z", description = "When the submission was made.")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime occurredAt;

  /**
   * How long this trainee took over the level, in seconds, carried only by a finding that turns on
   * solving speed and left unset otherwise
   */
  @Schema(
      example = "20",
      description = "Seconds the trainee took over the level; only a speed finding carries it.")
  private Long solvedInTime;

  @Schema(example = "John Doe")
  private String participantName;

  /**
   * The trainee's {@code userRefId}, the id spoken outside this service, rather than the local
   * primary key of the user row
   */
  @Schema(
      example = "6",
      description = "Cross-service user reference id of the trainee, not a local user key.")
  private Long userId;

  @Schema(example = "3")
  private Long detectionEventId;
}
