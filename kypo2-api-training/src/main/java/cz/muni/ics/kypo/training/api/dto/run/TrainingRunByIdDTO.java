package cz.muni.ics.kypo.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.SandboxInstanceRefDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.enums.TRState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * Encapsulates information about Training Run.
 *
 * @author Boris Jadus
 */
@ApiModel(value = "TrainingRunByIdDTO", description = "The act, or a recording, of performing actions during training from a perspective of one concrete participant.")
public class TrainingRunByIdDTO {

	private Long id;
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime startTime;
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime endTime;
	private String eventLogReference;
	private TRState state;
	private SandboxInstanceRefDTO sandboxInstanceRef;
	private UserRefDTO participantRef;
	private Long definitionId;
	private Long instanceId;
	private Long previousSandboxInstanceRefId;

	/**
	 * Gets id.
	 *
	 * @return the id
	 */
	@ApiModelProperty(value = "Main identifier of training run.", example = "1")
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
	@ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
	public TRState getState() {
		return state;
	}

	/**
	 * Sets state.
	 *
	 * @param state the {@link TRState}
	 */
	public void setState(TRState state) {
		this.state = state;
	}

	/**
	 * Gets sandbox instance reference.
	 *
	 * @return the {@link SandboxInstanceRefDTO}
	 */
	@ApiModelProperty(value = "Reference to the received sandbox.")
	public SandboxInstanceRefDTO getSandboxInstanceRef() {
		return sandboxInstanceRef;
	}

	/**
	 * Sets sandbox instance reference.
	 *
	 * @param sandboxInstanceRef the {@link SandboxInstanceRefDTO}
	 */
	public void setSandboxInstanceRef(SandboxInstanceRefDTO sandboxInstanceRef) {
		this.sandboxInstanceRef = sandboxInstanceRef;
	}

	/**
	 * Gets participant ref.
	 *
	 * @return the {@link UserRefDTO}
	 */
	@ApiModelProperty(value = "Reference to participant of training run.")
	public UserRefDTO getParticipantRef() {
		return participantRef;
	}

	/**
	 * Sets participant ref.
	 *
	 * @param participantRef the {@link UserRefDTO}
	 */
	public void setParticipantRef(UserRefDTO participantRef) {
		this.participantRef = participantRef;
	}

	/**
	 * Gets definition id.
	 *
	 * @return the definition id
	 */
	@ApiModelProperty(value = "Id of associated training definition")
	public Long getDefinitionId() {
		return definitionId;
	}

	/**
	 * Sets definition id.
	 *
	 * @param definitionId the definition id
	 */
	public void setDefinitionId(Long definitionId) {
		this.definitionId = definitionId;
	}

	/**
	 * Gets instance id.
	 *
	 * @return the instance id
	 */
	@ApiModelProperty(value = "Id of associated training instance")
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
	 * Gets ID of previous used sandbox instance ref.
	 *
	 * @return the previous sandbox instance ref id
	 */
	@ApiModelProperty(value = "Id of a previous sandbox instance assigned to the training run.", example = "12")
	public Long getPreviousSandboxInstanceRefId() {
		return previousSandboxInstanceRefId;
	}

	/**
	 * Sets ID of previous used sandbox instance ref.
	 *
	 * @param previousSandboxInstanceRefId the previous sandbox instance ref id
	 */
	public void setPreviousSandboxInstanceRefId(Long previousSandboxInstanceRefId) {
		this.previousSandboxInstanceRefId = previousSandboxInstanceRefId;
	}

	@Override
	public String toString() {
		return "TrainingRunByIdDTO{" +
				"id=" + id +
				", startTime=" + startTime +
				", endTime=" + endTime +
				", eventLogReference='" + eventLogReference + '\'' +
				", state=" + state +
				", sandboxInstanceRef=" + sandboxInstanceRef +
				", participantRef=" + participantRef +
				", definitionId=" + definitionId +
				", instanceId=" + instanceId +
				", previousSandboxInstanceRefId=" + previousSandboxInstanceRefId +
				'}';
	}
}
