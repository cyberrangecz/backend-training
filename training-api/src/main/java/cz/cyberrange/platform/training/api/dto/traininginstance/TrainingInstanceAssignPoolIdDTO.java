package cz.cyberrange.platform.training.api.dto.traininginstance;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

/** Encapsulates information about Training Instance, intended for assigning pool id */
@Data
@Schema(description = "The sandbox pool to assign to a training instance")
public class TrainingInstanceAssignPoolIdDTO {

  /**
   * Read directly by the facade, not through the mapper: locked with the instance's existing access
   * token via the sandbox service, then stored as the instance's pool id. Rejected when the
   * instance already carries a pool id or has local sandboxes enabled.
   */
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
  @NotNull(message = "{assignPool.poolId.NotNull.message}")
  private Long poolId;
}
