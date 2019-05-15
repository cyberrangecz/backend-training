package cz.muni.ics.kypo.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.SandboxInstanceRefDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.enums.TRState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

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

	@ApiModelProperty(value = "Id of associated training definition")
	public Long getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(Long definitionId) {
		this.definitionId = definitionId;
	}

	@ApiModelProperty(value = "Id of associated training instance")
	public Long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(Long instanceId) {
		this.instanceId = instanceId;
	}

}
