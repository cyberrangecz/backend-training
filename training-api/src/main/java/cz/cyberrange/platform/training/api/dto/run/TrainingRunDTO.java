package cz.cyberrange.platform.training.api.dto.run;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about Training Run. */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "TrainingRunDTO",
    description =
        "The act, or a recording, of performing actions during training from a perspective of one concrete participant.")
public class TrainingRunDTO extends TrainingRunBasicDTO {

  private String eventLogReference;

  @ApiModelProperty(value = "Allocation id to the received sandbox.")
  private Integer sandboxInstanceAllocationId;

  @ApiModelProperty(value = "Boolean to check whether event logging works.", example = "true")
  private boolean eventLoggingState;

  @ApiModelProperty(value = "Boolean to check whether command logging works.", example = "true")
  private boolean commandLoggingState;

  @ApiModelProperty(
      value = "Boolean to check whether the run has any detection events logged",
      example = "true")
  private boolean hasDetectionEvent;
}
