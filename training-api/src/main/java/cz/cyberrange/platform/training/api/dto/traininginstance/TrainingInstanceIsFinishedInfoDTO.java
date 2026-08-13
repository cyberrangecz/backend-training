package cz.cyberrange.platform.training.api.dto.traininginstance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** Encapsulates the information stating if training instance has finished. */
@Data
@ApiModel(
    value = "TrainingInstanceIsFinishedInfoDTO",
    description = "Information stating if training instance has finished.")
public class TrainingInstanceIsFinishedInfoDTO {

  @ApiModelProperty(value = "Sign if training instance is finished or not.", example = "false")
  private boolean hasFinished;

  @ApiModelProperty(value = "Message about training instance state.", example = "false")
  private String message;
}
