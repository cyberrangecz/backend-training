package cz.muni.ics.kypo.training.api.dto.run;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.SandboxInstanceRefDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
	private AbstractLevelDTO currentLevel;
	private TrainingInstanceDTO trainingInstance;
	private SandboxInstanceRefDTO sandboxInstanceRef;

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

	public AbstractLevelDTO getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(AbstractLevelDTO currentLevel) {
		this.currentLevel = currentLevel;
	}

	public TrainingInstanceDTO getTrainingInstance() {
		return trainingInstance;
	}

	public void setTrainingInstance(TrainingInstanceDTO trainingInstance) {
		this.trainingInstance = trainingInstance;
	}

	public SandboxInstanceRefDTO getSandboxInstanceRef() {
		return sandboxInstanceRef;
	}

	public void setSandboxInstanceRef(SandboxInstanceRefDTO sandboxInstanceRef) {
		this.sandboxInstanceRef = sandboxInstanceRef;
	}

	@Override
	public String toString() {
		return "TrainingRunDTO{" + "id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", eventLogReference='"
				+ eventLogReference + '\'' + ", state=" + state + ", currentLevel=" + currentLevel + ", trainingInstance=" + trainingInstance
				+ ", sandboxInstanceRef=" + sandboxInstanceRef + '}';
	}
}
