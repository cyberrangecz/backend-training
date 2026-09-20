package cz.cyberrange.platform.training.api.dto.archive;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.enums.TRState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Snapshot of one participant's training run, written as a single JSON file into the training runs
 * folder of the training instance archive
 */
@Data
@Schema(description = "One participant's training run as recorded in the instance archive")
public class TrainingRunArchiveDTO {

  @Schema(example = "1")
  private Long id;

  /**
   * Primary key of the training instance this run belongs to. The training run entity carries no
   * matching property, so the archiving facade sets this field itself after mapping the run.
   */
  @Schema(example = "1")
  private Long instanceId;

  @Schema(example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime startTime;

  @Schema(example = "2022-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime endTime;

  private String eventLogReference;

  @Schema(example = "ALLOCATED")
  private TRState state;

  /**
   * User reference id ({@code UserRef.userRefId}) of the run's participant, not the participant's
   * local primary key
   */
  @Schema(description = "The id the participant is known by outside this service.", example = "5")
  private Long participantRefId;
}
