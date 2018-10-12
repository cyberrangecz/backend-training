package cz.muni.ics.kypo.training.api.dto.traininginstance;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeDeserializer;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@ApiModel(value = "TrainingInstanceUpdateDTO", description = "Training Instance to update.")
public class TrainingInstanceUpdateDTO {
	@NotNull(message = "{traininginstanceupdate.id.NotNull.message}")
	private Long id;
	@NotNull(message = "{traininginstanceupdate.startTime.NotNull.message}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime startTime;
	@NotNull(message = "{traininginstanceupdate.endTime.NotNull.message}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime endTime;
	@NotEmpty(message = "{traininginstanceupdate.title.NotEmpty.message}")
	private String title;
	@NotNull(message = "{traininginstanceupdate.poolSize.NotNull.message}")
	@Min(value = 1, message = "{traininginstanceupdate.poolSize.Min.message}")
	@Max(value = 100, message = "{traininginstanceupdate.poolSize.Max.message}")
	private int poolSize;
	//@NotEmpty(message = "{traininginstanceupdate.keyword.NotEmpty.message}")
	private String keyword;
	@NotNull(message = "{traininginstanceupdate.trainingDefinition.NotNull.message}")
	private TrainingDefinitionDTO trainingDefinition;
	@NotNull(message = "{traininginstanceupdate.organizers.NotNull.message}")
	private Set<UserRefDTO> organizers;

	public TrainingInstanceUpdateDTO() {}

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

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
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
		return "TrainingInstanceUpdateDTO{" + "id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", title='" + title + '\''
				+ ", poolSize=" + poolSize + ", keyword='" + keyword + '\'' + ", trainingDefinition=" + trainingDefinition + ", organizers="
				+ organizers + '}';
	}
}
