package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.List;

/**
 * Encapsulates information about minimal solve time detection event.
 */
@ApiModel(value = "MinimalSolveTimeDetectionEventDTO", description = "A detection event of type Minimal Solve Time.", parent = AbstractDetectionEventDTO.class)
public class MinimalSolveTimeDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "Minimal time required to solve the level.", example = "1")
    private Long minimalSolveTime;

    @ApiModelProperty(value = "Participants of the detection event.", example = "1")
    private List<DetectionEventParticipantDTO> participants;

    public Long getMinimalSolveTime() {
        return minimalSolveTime;
    }

    public void setMinimalSolveTime(Long minimalSolveTime) {
        this.minimalSolveTime = minimalSolveTime;
    }

    public List<DetectionEventParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<DetectionEventParticipantDTO> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MinimalSolveTimeDetectionEventDTO that = (MinimalSolveTimeDetectionEventDTO) o;
        return Objects.equals(minimalSolveTime, that.minimalSolveTime) &&
                Objects.equals(participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minimalSolveTime, participants);
    }

    @Override
    public String toString() {
        return "MinimalSolveTimeDetectionEventDTO{" +
                "minimalSolveTime=" + minimalSolveTime +
                ", participants=" + participants +
                '}';
    }
}
