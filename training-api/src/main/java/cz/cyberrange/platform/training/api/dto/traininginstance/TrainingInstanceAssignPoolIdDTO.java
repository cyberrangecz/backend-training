package cz.cyberrange.platform.training.api.dto.traininginstance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Encapsulates information about Training Instance, intended for assigning pool id.
 *
 */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingInstanceAssignPoolIdDTO", description = "Training Instance assign pool ID.")
public class TrainingInstanceAssignPoolIdDTO {

    @ApiModelProperty(value = "Pool associated with training instance.", example = "2", required = true)
    @NotNull(message = "{assignPool.poolId.NotNull.message}")
    private Long poolId;
}
