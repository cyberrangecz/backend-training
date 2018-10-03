package cz.muni.ics.kypo.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeDeserializer;
import cz.muni.ics.kypo.training.utils.converters.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;

import java.time.LocalDateTime;

@ApiModel(value = "NewTrainingInstanceDTO", description = "Newly created Training Instance.")
public class NewTrainingInstanceDTO {

	private Long id;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime startTime;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime endTime;
	private String title;
	private int poolSize;
	private String keyword;

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

	@Override public String toString() {
		return "NewTrainingInstanceDTO{" + "id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", title='" + title + '\''
				+ ", poolSize=" + poolSize + ", keyword='" + keyword + '\'' + '}';
	}
}
