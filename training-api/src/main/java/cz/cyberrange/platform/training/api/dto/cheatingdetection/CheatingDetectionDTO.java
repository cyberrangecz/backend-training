package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.enums.CheatingDetectionState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * One run of the cheating detections over a training instance: who started it, when, how each of
 * the six kinds of detection is progressing, and how much it has turned up
 */
@Data
@Schema(description = "One run of the cheating detections over a training instance.")
public class CheatingDetectionDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  private Long trainingInstanceId;

  /** Display name of the user who started the run, as the user service reported it at the time */
  @Schema(
      example = "John Doe",
      description = "Name of the user who started the run; the server fills it in.")
  private String executedBy;

  @Schema(
      example = "2022-01-01T05:55:23Z",
      description = "When the run started; the server fills it in.")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime executeTime;

  /**
   * How close together two solves must fall before the time proximity detection treats them as
   * suspicious. It governs that one detection alone.
   */
  @Schema(
      example = "120",
      description = "Seconds within which two solves count as close; defaults to 120 if unset.")
  private Long proximityThreshold;

  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      accessMode = Schema.AccessMode.READ_ONLY,
      example = "1")
  private Long id;

  /** Where the run as a whole stands, moved on as the individual detections are worked through */
  @Schema(example = "RUNNING", description = "How far the run as a whole has got.")
  private CheatingDetectionState currentState;

  /** How many findings the run has recorded so far; zero until the first detection reports */
  @Schema(
      example = "20",
      description = "How many findings the run has recorded; the server fills it in.")
  private Long results;

  @Schema(example = "RUNNING")
  private CheatingDetectionState answerSimilarityState;

  @Schema(example = "RUNNING")
  private CheatingDetectionState locationSimilarityState;

  @Schema(example = "RUNNING")
  private CheatingDetectionState timeProximityState;

  @Schema(example = "RUNNING")
  private CheatingDetectionState minimalSolveTimeState;

  @Schema(example = "RUNNING")
  private CheatingDetectionState forbiddenCommandsState;

  @Schema(example = "RUNNING")
  private CheatingDetectionState noCommandsState;

  /**
   * The commands this run treats as forbidden. Empty leaves the forbidden commands detection with
   * nothing to match against.
   */
  @Schema(example = "[]", description = "Commands this run treats as forbidden.")
  private List<@NotNull @Valid ForbiddenCommandDTO> forbiddenCommands;
}
