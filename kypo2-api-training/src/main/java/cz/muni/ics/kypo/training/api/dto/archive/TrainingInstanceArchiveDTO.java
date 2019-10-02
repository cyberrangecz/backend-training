package cz.muni.ics.kypo.training.api.dto.archive;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.export.UserRefExportDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Encapsulates information about Training instance intended for export.
 *
 * @author Boris Jadus
 */
public class TrainingInstanceArchiveDTO {

	private Long id;
	private Long definitionId;
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime startTime;
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime endTime;
	private String title;
	private int poolSize;
	private Set<Long> organizersRefIds;
	private String accessToken;

	@ApiModelProperty(value = "Main identifier of training instance.", example = "1")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ApiModelProperty(value = "Main identifier of training definition associated with this instance.", example = "1")
	public Long getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(Long definitionId) {
		this.definitionId = definitionId;
	}

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
	 * Gets organizersRefIds of training instance.
	 *
	 * @return the organizersRefIds
	 */
	@ApiModelProperty(value = "Reference to organizersRefIds which organize training instance.")
	public Set<Long> getOrganizersRefIds() {
		return organizersRefIds;
	}

	/**
	 * Sets organizersRefIds of training instance.
	 *
	 * @param organizersRefIds the organizersRefIds
	 */
	public void setOrganizersRefIds(Set<Long> organizersRefIds) {
		this.organizersRefIds = organizersRefIds;
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

	@Override
	public String toString() {
		return "TrainingInstanceArchiveDTO{" +
				"id=" + id +
				", definitionId=" + definitionId +
				", startTime=" + startTime +
				", endTime=" + endTime +
				", title='" + title + '\'' +
				", poolSize=" + poolSize +
				", organizersRefIds=" + organizersRefIds +
				", accessToken='" + accessToken + '\'' +
				'}';
	}
}
