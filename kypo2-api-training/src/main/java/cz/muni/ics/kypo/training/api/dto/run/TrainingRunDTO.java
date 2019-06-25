package cz.muni.ics.kypo.training.api.dto.run;

import cz.muni.ics.kypo.training.api.dto.SandboxInstanceRefDTO;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.enums.TRState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "TrainingRunDTO", description = "The act, or a recording, of performing actions during training from a perspective of one concrete participant.")
public class TrainingRunDTO {

    private Long id;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime startTime;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime endTime;
    private String eventLogReference;
    private TRState state;
    private SandboxInstanceRefDTO sandboxInstanceRef;
    private UserRefDTO participantRef;

    @ApiModelProperty(value = "Main identifier of training run.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @ApiModelProperty(value = "Date when training run ends.", example = "2022-10-19 10:23:54+02")
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getEventLogReference() {
        return eventLogReference;
    }

    public void setEventLogReference(String eventLogReference) {
        this.eventLogReference = eventLogReference;
    }

    @ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
    public TRState getState() {
        return state;
    }

    public void setState(TRState state) {
        this.state = state;
    }

    @ApiModelProperty(value = "Reference to the received sandbox.")
    public SandboxInstanceRefDTO getSandboxInstanceRef() {
        return sandboxInstanceRef;
    }

    public void setSandboxInstanceRef(SandboxInstanceRefDTO sandboxInstanceRef) {
        this.sandboxInstanceRef = sandboxInstanceRef;
    }

    @ApiModelProperty(value = "Reference to participant of training run.")
    public UserRefDTO getParticipantRef() {
        return participantRef;
    }

    public void setParticipantRef(UserRefDTO participantRef) {
        this.participantRef = participantRef;
    }

    @Override
    public String toString() {
        return "TrainingRunDTO{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", eventLogReference='" + eventLogReference + '\'' +
                ", state=" + state +
                ", sandboxInstanceRef=" + sandboxInstanceRef +
                ", participantRef=" + participantRef +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TrainingRunDTO)) return false;
        TrainingRunDTO that = (TrainingRunDTO) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getState(), that.getState()) &&
                Objects.equals(getParticipantRef(), that.getParticipantRef());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getState(), getParticipantRef());
    }
}
