package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

/**
 * Encapsulates information about time proximity detection event.
 */
@ApiModel(value = "TimeProximityDetectionEventDTO", description = "A detection event of type Time Proximity.", parent = AbstractDetectionEventDTO.class)
public class TimeProximityDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "Time threshold for detection.", example = "1")
    private Long threshold;
    @ApiModelProperty(value = "Participants of the detection event.", example = "1")
    private List<DetectionEventParticipantDTO> participants;

    public Long getThreshold() {
        return threshold;
    }

    public void setThreshold(Long threshold) {
        this.threshold = threshold;
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
        TimeProximityDetectionEventDTO that = (TimeProximityDetectionEventDTO) o;
        return Objects.equals(threshold, that.threshold) && Objects.equals(participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), threshold, participants);
    }

    @Override
    public String toString() {
        return "TimeProximityDetectionEventDTO{" +
                "threshold=" + threshold +
                ", participants=" + participants +
                '}';
    }
}
