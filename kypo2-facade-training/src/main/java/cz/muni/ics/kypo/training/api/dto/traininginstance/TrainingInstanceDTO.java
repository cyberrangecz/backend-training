package cz.muni.ics.kypo.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@ApiModel(value = "TrainingInstanceDTO", description = "Training Instance.")
public class TrainingInstanceDTO {

	private Long id;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime startTime;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime endTime;
	private String title;
	private int poolSize;
	private TrainingDefinitionDTO trainingDefinition;
	private Set<UserRefDTO> organizers;

	public TrainingInstanceDTO() {}

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public TrainingDefinitionDTO getTrainingDefinition() {
		return trainingDefinition;
	}

	public void setTrainingDefinition(TrainingDefinitionDTO trainingDefinition) {
		this.trainingDefinition = trainingDefinition;
	}

	public Set<UserRefDTO> getOrganizers() {
		return organizers;
	}

	public void setOrganizers(Set<UserRefDTO> organizers) {
		this.organizers = organizers;
	}

	@Override public String toString() {
		return "TrainingInstanceDTO{" + "id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", title='" + title + '\''
				+ ", poolSize=" + poolSize + ", trainingDefinition=" + trainingDefinition + ", organizers=" + organizers + '}';
	}
}
