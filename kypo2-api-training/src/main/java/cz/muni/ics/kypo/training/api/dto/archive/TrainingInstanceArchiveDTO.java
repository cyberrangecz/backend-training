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

/**
 * Encapsulates information about Training instance, its definition with levels and associated runs intended for export.
 *
 * @author Boris Jadus
 */
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

	/**
	 * Gets start time of training instance.
	 *
	 * @return the start time
	 */
	@ApiModelProperty(value = "Date when training instance starts.", example = "2016-10-19 10:23:54+02")
	public LocalDateTime getStartTime() {
		return startTime;
	}

	/**
	 * Sets start time of training instance.
	 *
	 * @param startTime the start time
	 */
	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	/**
	 * Gets end time of training instance.
	 *
	 * @return the end time
	 */
	@ApiModelProperty(value = "Date when training instance ends.", example = "2017-10-19 10:23:54+02")
	public LocalDateTime getEndTime() {
		return endTime;
	}

	/**
	 * Sets end time of training instance.
	 *
	 * @param endTime the end time
	 */
	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets title of training instance.
	 *
	 * @return the title
	 */
	@ApiModelProperty(value = "Short textual description of the training instance.", example = "Concluded Instance")
	public String getTitle() {
		return title;
	}

	/**
	 * Sets title of training instance.
	 *
	 * @param title the title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets pool size of training instance.
	 *
	 * @return the pool size
	 */
	@ApiModelProperty(value = "Number of sandboxes that can be allocated.", example = "5")
	public int getPoolSize() {
		return poolSize;
	}

	/**
	 * Sets pool size of training instance.
	 *
	 * @param poolSize the pool size
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * Gets organizers of training instance.
	 *
	 * @return the organizers
	 */
	@ApiModelProperty(value = "Reference to organizers which organize training instance.")
	public Set<UserRefExportDTO> getOrganizers() {
		return organizers;
	}

	/**
	 * Sets organizers of training instance.
	 *
	 * @param organizers the organizers
	 */
	public void setOrganizers(Set<UserRefExportDTO> organizers) {
		this.organizers = organizers;
	}

	/**
	 * Gets access token of training instance.
	 *
	 * @return the access token
	 */
	@ApiModelProperty(value = "Token needed to access runs created from this definition", example = "pass-1234")
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * Sets access token of training instance.
	 *
	 * @param accessToken the access token
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * Gets training definition and its levels associated with training instance.
	 *
	 * @return the {@link ExportTrainingDefinitionAndLevelsDTO}
	 */
	@ApiModelProperty(value = "Training definition with levels on which this instance is based")
	public ExportTrainingDefinitionAndLevelsDTO getExportTrainingDefinitionAndLevelsDTO() {
		return exportTrainingDefinitionAndLevelsDTO;
	}

	/**
	 * Sets training definition and levels of training instance.
	 *
	 * @param exportTrainingDefinitionAndLevelsDTO a training definition and levels
	 */
	public void setExportTrainingDefinitionAndLevelsDTO(ExportTrainingDefinitionAndLevelsDTO exportTrainingDefinitionAndLevelsDTO) {
		this.exportTrainingDefinitionAndLevelsDTO = exportTrainingDefinitionAndLevelsDTO;
	}

	/**
	 * Gets training runs associated with training instance.
	 *
	 * @return the training runs
	 */
	@ApiModelProperty(value = "Training runs based on this instance")
	public Set<TrainingRunExportDTO> getTrainingRuns() {
		return trainingRuns;
	}

	/**
	 * Sets training runs associated with training instance.
	 *
	 * @param trainingRuns the training runs
	 */
	public void setTrainingRuns(Set<TrainingRunExportDTO> trainingRuns) {
		this.trainingRuns = trainingRuns;
	}

	@Override public String toString() {
		return "TrainingInstanceArchiveDTO{" + "startTime=" + startTime + ", endTime=" + endTime + ", title='" + title + '\'' + ", poolSize="
				+ poolSize + ", organizers=" + organizers + ", accessToken='" + accessToken + '\'' + ", exportTrainingDefinitionAndLevelsDTO="
				+ exportTrainingDefinitionAndLevelsDTO + ", trainingRuns=" + trainingRuns + '}';
	}
}
