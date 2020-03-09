package cz.muni.ics.kypo.training.api.dto.traininginstance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * Encapsulates information about Training Instance, intended for updating or assignin pool id.
 *
 */
@ApiModel(value = "TrainingInstanceAssignPoolIdDTO", description = "Training Instance assign pool ID.")
public class TrainingInstanceAssignPoolIdDTO {

    @ApiModelProperty(value = "Main identifier of training instance.", example = "1", required = true)
    @NotNull
    private Long id;
    @ApiModelProperty(value = "Pool associated with training instance.", example = "2", required = true)
    @NotNull
    private Long poolId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPoolId() {
        return poolId;
    }

    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    @Override
    public String toString() {
        return "TrainingInstanceAssignPoolIdDTO{" +
                "id=" + id +
                ", poolId=" + poolId +
                '}';
    }
}
