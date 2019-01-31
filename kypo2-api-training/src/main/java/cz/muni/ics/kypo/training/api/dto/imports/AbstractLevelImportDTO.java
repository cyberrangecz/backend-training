package cz.muni.ics.kypo.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "AbstractLevelImportDTO", subTypes = {GameLevelImportDTO.class, InfoLevelImportDTO.class, AssessmentLevelImportDTO.class},
		description = "Superclass for classes GameLevelImportDTO, AssessmentLevelImportDTO and InfoLevelImportDTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = GameLevelImportDTO.class, name = "GameLevelImportDTO"),
		@JsonSubTypes.Type(value = AssessmentLevelImportDTO.class, name = "AssessmentLevelImportDTO"),
		@JsonSubTypes.Type(value = InfoLevelImportDTO.class, name = "InfoLevelImportDTO")})
public abstract class AbstractLevelImportDTO {
	@NotEmpty(message = "{abstractlevelimport.title.NotEmpty.message}")
	protected String title;
	@NotNull(message = "{abstractlevelimport.maxScore.NotNull.message}")
	@Min(value = 0, message = "{abstractlevelimport.maxScore.Min.message}")
	@Max(value = 100, message = "{abstractlevelimport.maxScore.Max.message}")
	protected int maxScore;
	@NotNull(message = "{abstractlevelimport.type.NotNull.message}")
	protected LevelType levelType;
	@NotNull(message = "{abstractlevelimport.order.NotNull.message}")
	protected int order;

	public AbstractLevelImportDTO() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}

	public LevelType getLevelType() {
		return levelType;
	}

	public void setLevelType(LevelType levelType) {
		this.levelType = levelType;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override public String toString() {
		return "AbstractLevelImportDTO{" + "title='" + title + '\'' + ", maxScore=" + maxScore + ", levelType=" + levelType + ", order=" + order
				+ '}';
	}
}
