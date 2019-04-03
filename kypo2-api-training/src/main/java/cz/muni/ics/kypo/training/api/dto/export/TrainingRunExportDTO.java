package cz.muni.ics.kypo.training.api.dto.export;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cz.muni.ics.kypo.training.api.enums.TRState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

public class TrainingRunExportDTO {

	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime startTime;
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime endTime;
	private String eventLogReference;
	private TRState state;
	//private SandboxInstanceRefDTO sandboxInstanceRef;
	private UserRefExportDTO participantRef;

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

	public TRState getState() {
		return state;
	}

	@ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
	public void setState(TRState state) {
		this.state = state;
	}

	@ApiModelProperty(value = "Reference to participant of training run.")
	public UserRefExportDTO getParticipantRef() {
		return participantRef;
	}

	public void setParticipantRef(UserRefExportDTO participantRef) {
		this.participantRef = participantRef;
	}

	@Override public String toString() {
		return "TrainingRunExportDTO{" + "startTime=" + startTime + ", endTime=" + endTime + ", eventLogReference='" + eventLogReference + '\''
				+ ", state=" + state + ", participantRef=" + participantRef + '}';
	}
}


