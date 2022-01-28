package cz.muni.ics.kypo.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Encapsulates information about abstract level.
 * Extended by {@link AssessmentLevelImportDTO}, {@link TrainingLevelImportDTO}, {@link AccessLevelImportDTO} and {@link InfoLevelImportDTO}
 *
 */
@ApiModel(value = "AbstractLevelImportDTO", subTypes = {TrainingLevelImportDTO.class, AccessLevelImportDTO.class, InfoLevelImportDTO.class, AssessmentLevelImportDTO.class},
		description = "Superclass for classes TrainingLevelImportDTO, AccessLevelImportDTO, AssessmentLevelImportDTO and InfoLevelImportDTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "level_type", visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = TrainingLevelImportDTO.class, name = "TRAINING_LEVEL"),
		@JsonSubTypes.Type(value = TrainingLevelImportDTO.class, name = "GAME_LEVEL"),
		@JsonSubTypes.Type(value = AccessLevelImportDTO.class, name = "ACCESS_LEVEL"),
		@JsonSubTypes.Type(value = AssessmentLevelImportDTO.class, name = "ASSESSMENT_LEVEL"),
		@JsonSubTypes.Type(value = InfoLevelImportDTO.class, name = "INFO_LEVEL")})
public class AbstractLevelImportDTO {

	@ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
	@NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
	protected String title;
	@ApiModelProperty(value = "Type of the level.", example = "TRAINING_LEVEL")
	@NotNull(message = "{abstractLevel.type.NotNull.message}")
	protected LevelType levelType;
	@ApiModelProperty(value = "Order of level, starts with 0", example = "2")
	@NotNull(message = "{abstractLevel.order.NotNull.message}")
	@Min(value = 0, message = "{abstractLevel.order.Min.message}")
	protected Integer order;
	@ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", example = "5")
	@NotNull(message = "{abstractLevel.estimatedDuration.NotNull.message}")
	@Min(value = 0, message = "{abstractLevel.estimatedDuration.Min.message}")
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
		return "AbstractLevelImportDTO{" + "title='" + title + '\'' + ", levelType=" + levelType + ", order=" + order
				+ ", estimatedDuration=" + estimatedDuration + '}';
	}
}
