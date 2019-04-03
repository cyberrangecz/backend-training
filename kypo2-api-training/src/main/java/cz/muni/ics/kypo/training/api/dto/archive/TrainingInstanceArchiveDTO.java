package cz.muni.ics.kypo.training.api.dto.archive;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.export.TrainingRunExportDTO;
import cz.muni.ics.kypo.training.api.dto.export.UserRefExportDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class TrainingInstanceArchiveDTO {

	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime startTime;
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime endTime;
	private String title;
	private int poolSize;
	private Set<UserRefExportDTO> organizers;
	private String accessToken;
	private ExportTrainingDefinitionAndLevelsDTO exportTrainingDefinitionAndLevelsDTO;
	private Set<TrainingRunExportDTO> trainingRuns = new HashSet<>();

	@ApiModelProperty(value = "Date when training instance starts.", example = "2016-10-19 10:23:54+02")
	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	@ApiModelProperty(value = "Date when training instance ends.", example = "2017-10-19 10:23:54+02")
	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	@ApiModelProperty(value = "Short textual description of the training instance.", example = "Concluded Instance")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@ApiModelProperty(value = "Number of sandboxes that can be allocated.", example = "5")
	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	@ApiModelProperty(value = "Reference to organizers which organize training instance.")
	public Set<UserRefExportDTO> getOrganizers() {
		return organizers;
	}

	public void setOrganizers(Set<UserRefExportDTO> organizers) {
		this.organizers = organizers;
	}

	@ApiModelProperty(value = "Token needed to access runs created from this definition", example = "pass-1234")
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@ApiModelProperty(value = "Training definition with levels on which this instance is based")
	public ExportTrainingDefinitionAndLevelsDTO getExportTrainingDefinitionAndLevelsDTO() {
		return exportTrainingDefinitionAndLevelsDTO;
	}

	public void setExportTrainingDefinitionAndLevelsDTO(ExportTrainingDefinitionAndLevelsDTO exportTrainingDefinitionAndLevelsDTO) {
		this.exportTrainingDefinitionAndLevelsDTO = exportTrainingDefinitionAndLevelsDTO;
	}

	@ApiModelProperty(value = "Training runs based on this instance")
	public Set<TrainingRunExportDTO> getTrainingRuns() {
		return trainingRuns;
	}

	public void setTrainingRuns(Set<TrainingRunExportDTO> trainingRuns) {
		this.trainingRuns = trainingRuns;
	}

	@Override public String toString() {
		return "TrainingInstanceArchiveDTO{" + "startTime=" + startTime + ", endTime=" + endTime + ", title='" + title + '\'' + ", poolSize="
				+ poolSize + ", organizers=" + organizers + ", accessToken='" + accessToken + '\'' + ", exportTrainingDefinitionAndLevelsDTO="
				+ exportTrainingDefinitionAndLevelsDTO + ", trainingRuns=" + trainingRuns + '}';
	}
}
