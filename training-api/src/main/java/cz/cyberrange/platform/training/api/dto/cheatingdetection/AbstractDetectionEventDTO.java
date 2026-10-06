package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.enums.DetectionEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * One finding of a cheating detection run, holding what every kind of finding has in common: the
 * detection it came from, the training run and level whose submission triggered it, and the people
 * implicated. Each kind of finding adds its own evidence in a subtype.
 */
@Data
@Schema(
    description =
        "One finding of a cheating detection run, with the level and people it implicates.",
    subTypes = {
      AnswerSimilarityDetectionEventDTO.class,
      ForbiddenCommandsDetectionEventDTO.class,
      LocationSimilarityDetectionEventDTO.class,
      MinimalSolveTimeDetectionEventDTO.class,
      NoCommandsDetectionEventDTO.class,
      TimeProximityDetectionEventDTO.class
    })
public class AbstractDetectionEventDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "1")
  private Long trainingInstanceId;

  @Schema(example = "2")
  private Long cheatingDetectionId;

  @Schema(example = "2")
  private Long trainingRunId;

  @Schema(example = "3")
  private Long levelId;

  @Schema(example = "3")
  private int levelOrder;

  @Schema(example = "SQL injection")
  private String levelTitle;

  /**
   * The moment the detection run was executed, shared by every finding of that run, rather than the
   * moment the trainee did the thing being flagged
   */
  @Schema(
      example = "2022-01-01T05:55:23Z",
      description = "When the detection run executed, not when the flagged thing happened.")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime detectedAt;

  @Schema(example = "3")
  private Long participantCount;

  @Schema(example = "ANSWER_SIMILARITY")
  private DetectionEventType detectionEventType;

  /** The implicated people's display names, run together into one comma-separated string */
  @Schema(
      example = "John Doe,Jane Doe",
      description = "The names of the implicated people, joined into one comma-separated string.")
  private String participants;
}
