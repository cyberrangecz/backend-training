package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
   * Epoch-millisecond instant the training run started, copied unchanged from the audit document;
   * unlike {@link AbstractEventDTO#getTimestamp()} it is not converted to a {@code LocalDateTime}
   */
  @JsonProperty("start_time")
  private Long startTime;

  /**
   * Epoch-millisecond instant the training run finished, copied unchanged from the audit document;
   * unlike {@link AbstractEventDTO#getTimestamp()} it is not converted to a {@code LocalDateTime}
   */
  @JsonProperty("end_time")
  private Long endTime;
}
