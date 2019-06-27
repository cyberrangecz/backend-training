package cz.muni.ics.kypo.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Encapsulates information about abstract level.
 * Extended by {@link AssessmentLevelImportDTO}, {@link GameLevelImportDTO} and {@link InfoLevelImportDTO}
 *
 * @author Boris Jadus(445343)
 */
@ApiModel(value = "AbstractLevelImportDTO", subTypes = {GameLevelImportDTO.class, InfoLevelImportDTO.class, AssessmentLevelImportDTO.class},
		description = "Superclass for classes GameLevelImportDTO, AssessmentLevelImportDTO and InfoLevelImportDTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = GameLevelImportDTO.class, name = "GameLevelImportDTO"),
		@JsonSubTypes.Type(value = AssessmentLevelImportDTO.class, name = "AssessmentLevelImportDTO"),
		@JsonSubTypes.Type(value = InfoLevelImportDTO.class, name = "InfoLevelImportDTO")})
public class AbstractLevelImportDTO {

	@NotEmpty(message = "{abstractlevelimport.title.NotEmpty.message}")
	protected String title;
	@NotNull(message = "{abstractlevelimport.maxScore.NotNull.message}")
	@Min(value = 0, message = "{abstractlevelimport.maxScore.Min.message}")
	@Max(value = 100, message = "{abstractlevelimport.maxScore.Max.message}")
	protected int maxScore;
	@NotNull(message = "{abstractlevelimport.type.NotNull.message}")
	protected LevelType levelType;
	@NotNull(message = "{abstractlevelimport.order.NotNull.message}")
	protected Integer order;
	protected Integer estimatedDuration;

	/**
	 * Instantiates a new Abstract level import dto.
	 */
	public AbstractLevelImportDTO() {
	}

	/**
	 * Gets title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets title.
	 *
	 * @param title the title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets max score.
	 *
	 * @return the max score
	 */
	public int getMaxScore() {
		return maxScore;
	}

	/**
	 * Sets max score.
	 *
	 * @param maxScore the max score
	 */
	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}

	/**
	 * Gets level type.
	 *
	 * @return the {@link LevelType}
	 */
	public LevelType getLevelType() {
		return levelType;
	}

	/**
	 * Sets level type.
	 *
	 * @param levelType the {@link LevelType}
	 */
	public void setLevelType(LevelType levelType) {
		this.levelType = levelType;
	}

	/**
	 * Gets order number of level that is compared with order numbers of other levels associated with same definition.
	 * First level from definition has order of 0
	 *
	 * @return the order
	 */
	public Integer getOrder() {
		return order;
	}

	/**
	 * Sets order number of level that is compared with order numbers of other levels associated with same definition.
	 * First level from definition has order of 0
	 *
	 * @param order the order
	 */
	public void setOrder(Integer order) {
		this.order = order;
	}

	/**
	 * Gets estimated duration.
	 *
	 * @return the estimated duration
	 */
	public Integer getEstimatedDuration() {
		return estimatedDuration;
	}

	/**
	 * Sets estimated duration.
	 *
	 * @param estimatedDuration the estimated duration
	 */
	public void setEstimatedDuration(Integer estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	@Override public String toString() {
		return "AbstractLevelImportDTO{" + "title='" + title + '\'' + ", maxScore=" + maxScore + ", levelType=" + levelType + ", order=" + order
				+ ", estimatedDuration=" + estimatedDuration + '}';
	}
}
