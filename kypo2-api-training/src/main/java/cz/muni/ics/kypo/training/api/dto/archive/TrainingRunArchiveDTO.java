package cz.muni.ics.kypo.training.api.dto.archive;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.enums.TRState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * Encapsulates information about Training run.
 * Used for archiving
 */
@ApiModel(value = "TrainingRunArchiveDTO", description = "An archived run of training instance of a particular participant.")
public class TrainingRunArchiveDTO {

    @ApiModelProperty(value = "Main identifier of training run.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Main identifier of training instance associated with this run.", example = "1")
    private Long instanceId;
    @ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime startTime;
    @ApiModelProperty(value = "Date when training run ends.", example = "2022-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime endTime;
    private String eventLogReference;
    @ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
    private TRState state;
    @ApiModelProperty(value = "Reference to participant of training run.", example = "5")
    private Long participantRefId;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets instance id.
     *
     * @return the instance id
     */
    public Long getInstanceId() {
        return instanceId;
    }

    /**
     * Sets instance id.
     *
     * @param instanceId the instance id
     */
    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Gets start time.
     *
     * @return the start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets end time.
     *
     * @return the end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Sets end time.
     *
     * @param endTime the end time
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets event log reference.
     *
     * @return the event log reference
     */
    public String getEventLogReference() {
        return eventLogReference;
    }

    /**
     * Sets event log reference.
     *
     * @param eventLogReference the event log reference
     */
    public void setEventLogReference(String eventLogReference) {
        this.eventLogReference = eventLogReference;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public TRState getState() {
        return state;
    }

    /**
     * Sets state.
     *
     * @param state the state
     */
    public void setState(TRState state) {
        this.state = state;
    }

    /**
     * Gets participant ref id.
     *
     * @return the participant ref id
     */
    public Long getParticipantRefId() {
        return participantRefId;
    }

    /**
     * Sets participant ref id.
     *
     * @param participantRefId the participant ref id
     */
    public void setParticipantRefId(Long participantRefId) {
        this.participantRefId = participantRefId;
    }

    @Override
    public String toString() {
        return "TrainingRunArchiveDTO{" +
                "id=" + id +
                ", instanceId=" + instanceId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", eventLogReference='" + eventLogReference + '\'' +
                ", state=" + state +
                ", participantRefId=" + participantRefId +
                '}';
    }
}
