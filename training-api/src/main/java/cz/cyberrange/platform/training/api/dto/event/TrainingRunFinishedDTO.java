package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "TrainingRunFinishedDTO", description = "Training run finished event")
public class TrainingRunFinishedDTO extends TrainingEventDTO {

  @ApiModelProperty(value = "Start time of the training run")
  @JsonProperty("start_time")
  private Long startTime;

  @ApiModelProperty(value = "End time of the training run")
  @JsonProperty("end_time")
  private Long endTime;
}
