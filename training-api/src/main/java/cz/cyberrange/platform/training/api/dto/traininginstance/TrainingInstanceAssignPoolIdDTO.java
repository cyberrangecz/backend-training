package cz.cyberrange.platform.training.api.dto.traininginstance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/** Encapsulates information about Training Instance, intended for assigning pool id */
@Data
@ApiModel(
    value = "TrainingInstanceAssignPoolIdDTO",
    description = "Training Instance assign pool ID.")
public class TrainingInstanceAssignPoolIdDTO {

  /**
   * Read directly by the facade, not through the mapper: locked with the instance's existing access
   * token via the sandbox service, then stored as the instance's pool id. Rejected when the
   * instance already carries a pool id or has local sandboxes enabled.
   */
  @ApiModelProperty(
      value = "Pool associated with training instance.",
      example = "2",
      required = true)
  @NotNull(message = "{assignPool.poolId.NotNull.message}")
  private Long poolId;
}
