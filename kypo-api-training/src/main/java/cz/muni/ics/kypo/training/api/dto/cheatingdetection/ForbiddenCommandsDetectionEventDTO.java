package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Encapsulates information about forbidden commands detection event.
 */
@ApiModel(value = "ForbiddenCommandsDetectionEventDTO", description = "A detection event of type Forbidden Commands.", parent = AbstractDetectionEventDTO.class)
public class ForbiddenCommandsDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "count of forbidden commands.", example = "10")
    private int commandCount;
    @ApiModelProperty(value = "id of training run.", example = "1")
    private Long trainingRunId;

    public int getCommandCount() {
        return commandCount;
    }

    public void setCommandCount(int commandCount) {
        this.commandCount = commandCount;
    }

    public Long getTrainingRunId() {
        return trainingRunId;
    }

    public void setTrainingRunId(Long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ForbiddenCommandsDetectionEventDTO that = (ForbiddenCommandsDetectionEventDTO) o;
        return commandCount == that.commandCount && trainingRunId == that.trainingRunId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), commandCount, trainingRunId);
    }

    @Override
    public String toString() {
        return "ForbiddenCommandsDetectionEventDTO{" +
                "commandCount=" + commandCount +
                "trainingRunId=" + trainingRunId +
                '}';
    }
}
