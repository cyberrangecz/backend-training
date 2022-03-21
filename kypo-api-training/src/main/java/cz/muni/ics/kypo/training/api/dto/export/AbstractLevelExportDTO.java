package cz.muni.ics.kypo.training.api.dto.export;

import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about abstract level.
 * Extended by {@link AssessmentLevelExportDTO}, {@link TrainingLevelExportDTO}, {@link AccessLevelExportDTO} and {@link InfoLevelExportDTO}
 *
 */
@ApiModel(value = "AbstractLevelExportDTO", subTypes = {TrainingLevelExportDTO.class, AccessLevelExportDTO.class, InfoLevelExportDTO.class, AssessmentLevelExportDTO.class},
        description = "Superclass for classes TrainingLevelExportDTO, AccessLevelExportDTO, InfoLevelExportDTO and AssessmentLevelExportDTO")
public class AbstractLevelExportDTO {

    @ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
    protected String title;
    @ApiModelProperty(value = "Type of the level.", example = "TRAINING")
    protected LevelType levelType;
    @ApiModelProperty(value = "Order of level, starts with 0", example = "2")
    protected int order;
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", example = "5")
    protected int estimatedDuration;
    @ApiModelProperty(value = "Minimal possible solve time (minutes) that must be taken by the player to solve the level.", example = "5")
    protected Integer minimalPossibleSolveTime;

    /**
     * Instantiates a new Abstract level export dto.
     */
    public AbstractLevelExportDTO(){}

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
     * Gets order.
     *
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets order.
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Gets estimated duration.
     *
     * @return the estimated duration
     */
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Sets estimated duration.
     *
     * @param estimatedDuration the estimated duration
     */
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * Gets minimal possible solve time.
     *
     * @return the minimal possible solve time
     */
    public Integer getMinimalPossibleSolveTime() {
        return minimalPossibleSolveTime;
    }

    /**
     * Sets minimal possible solve time.
     *
     * @param minimalPossibleSolveTime the minimal possible solve time
     */
    public void setMinimalPossibleSolveTime(Integer minimalPossibleSolveTime) {
        this.minimalPossibleSolveTime = minimalPossibleSolveTime;
    }

    @Override
    public String toString() {
        return "AbstractLevelExportDTO{" +
                "title='" + title + '\'' +
                ", levelType=" + levelType +
                ", order=" + order +
                ", estimatedDuration=" + estimatedDuration +
                ", minimalPossibleSolveTime=" + minimalPossibleSolveTime +
                '}';
    }
}
