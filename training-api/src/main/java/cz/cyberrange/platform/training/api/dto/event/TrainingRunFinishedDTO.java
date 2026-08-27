package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Training run finished event, carrying the {@code type} value {@code training_run_finished}. */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "TrainingRunFinishedDTO", description = "Training run finished event")
public class TrainingRunFinishedDTO extends TrainingEventDTO {

  /**
   * Epoch-millisecond instant the training run started, copied unchanged from the audit document;
   * unlike {@link AbstractEventDTO#getTimestamp()} it is not converted to a {@code LocalDateTime}.
   */
  @ApiModelProperty(value = "Start time of the training run")
  @JsonProperty("start_time")
  private Long startTime;

  /**
   * Epoch-millisecond instant the training run finished, copied unchanged from the audit document;
   * unlike {@link AbstractEventDTO#getTimestamp()} it is not converted to a {@code LocalDateTime}.
   */
  @ApiModelProperty(value = "End time of the training run")
  @JsonProperty("end_time")
  private Long endTime;
}
