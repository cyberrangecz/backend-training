package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Training run finished event, carrying the {@code type} value {@code training_run_finished} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Records that a training run reached its end.")
public class TrainingRunFinishedDTO extends TrainingEventDTO {

  /**
   * Moment the training run started, in UTC, converted from the epoch-millisecond value the audit
   * document stores
   */
  @JsonProperty("start_time")
  @Schema(example = "2021-03-24T11:00:00.000Z")
  private LocalDateTime startTime;

  /**
   * Moment the training run finished, in UTC, converted from the epoch-millisecond value the audit
   * document stores
   */
  @JsonProperty("end_time")
  @Schema(example = "2021-03-24T12:00:00.000Z")
  private LocalDateTime endTime;
}
