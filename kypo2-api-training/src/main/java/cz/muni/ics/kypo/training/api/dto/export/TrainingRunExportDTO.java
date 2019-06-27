package cz.muni.ics.kypo.training.api.dto.export;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cz.muni.ics.kypo.training.api.enums.TRState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * Encapsulates information about Training Run.
 */
public class TrainingRunExportDTO {

	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime startTime;
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime endTime;
	private String eventLogReference;
	private TRState state;
	private UserRefExportDTO participantRef;

	/**
	 * Gets start time.
	 *
	 * @return the start time
	 */
	@ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
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
	@ApiModelProperty(value = "Date when training run ends.", example = "2022-10-19 10:23:54+02")
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
	 * @return the {@link TRState}
	 */
	public TRState getState() {
		return state;
	}

	/**
	 * Sets state.
	 *
	 * @param state {@link TRState}
	 */
	@ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
	public void setState(TRState state) {
		this.state = state;
	}

	/**
	 * Gets participant ref.
	 *
	 * @return the {@link UserRefExportDTO}
	 */
	@ApiModelProperty(value = "Reference to participant of training run.")
	public UserRefExportDTO getParticipantRef() {
		return participantRef;
	}

	/**
	 * Sets participant ref.
	 *
	 * @param participantRef the {@link UserRefExportDTO}
	 */
	public void setParticipantRef(UserRefExportDTO participantRef) {
		this.participantRef = participantRef;
	}

	@Override public String toString() {
		return "TrainingRunExportDTO{" + "startTime=" + startTime + ", endTime=" + endTime + ", eventLogReference='" + eventLogReference + '\''
				+ ", state=" + state + ", participantRef=" + participantRef + '}';
	}
}


