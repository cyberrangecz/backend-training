package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

/**
 * Encapsulates information about no commands detection event.
 */
@ApiModel(value = "NoCommandsDetectionEventDTO", description = "A detection event of type No Commands.", parent = AbstractDetectionEventDTO.class)
public class NoCommandsDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "Participants of the detection event.", example = "1")
    private List<DetectionEventParticipantDTO> participants;

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
        NoCommandsDetectionEventDTO that = (NoCommandsDetectionEventDTO) o;
        return Objects.equals(participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), participants);
    }

    @Override
    public String toString() {
        return "NoCommandsDetectionEventDTO{" +
                "participants=" + participants +
                '}';
    }
}
