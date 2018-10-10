package cz.muni.ics.kypo.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.SandboxInstanceRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;

import java.time.LocalDateTime;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@ApiModel(value = "TrainingRunDTO", description = ".")
public class TrainingRunDTO {

	private Long id;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime startTime;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime endTime;
	private String eventLogReference;
	private TRState state;
	private AbstractLevel currentLevel;
	private TrainingInstance trainingInstance;
	private SandboxInstanceRef sandboxInstanceRef;

	public TrainingRunDTO() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

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

	public void setState(TRState state) {
		this.state = state;
	}

	public AbstractLevel getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(AbstractLevel currentLevel) {
		this.currentLevel = currentLevel;
	}

	public TrainingInstance getTrainingInstance() {
		return trainingInstance;
	}

	public void setTrainingInstance(TrainingInstance trainingInstance) {
		this.trainingInstance = trainingInstance;
	}

	public SandboxInstanceRef getSandboxInstanceRef() {
		return sandboxInstanceRef;
	}

	public void setSandboxInstanceRef(SandboxInstanceRef sandboxInstanceRef) {
		this.sandboxInstanceRef = sandboxInstanceRef;
	}

	@Override
	public String toString() {
		return "TrainingRunDTO{" + "id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", eventLogReference='"
				+ eventLogReference + '\'' + ", state=" + state + ", currentLevel=" + currentLevel + ", trainingInstance=" + trainingInstance
				+ ", sandboxInstanceRef=" + sandboxInstanceRef + '}';
	}
}
