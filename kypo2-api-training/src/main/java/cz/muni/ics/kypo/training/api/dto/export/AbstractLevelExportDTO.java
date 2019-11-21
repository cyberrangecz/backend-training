package cz.muni.ics.kypo.training.api.dto.export;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.muni.ics.kypo.training.api.dto.archive.AssessmentLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.archive.GameLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.archive.InfoLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about abstract level.
 * Extended by {@link AssessmentLevelExportDTO}, {@link GameLevelExportDTO} and {@link InfoLevelExportDTO}
 *
 * @author Pavel Seda
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = GameLevelExportDTO.class, name = "GameLevelImportDTO"),
    @JsonSubTypes.Type(value = AssessmentLevelExportDTO.class, name = "AssessmentLevelImportDTO"),
    @JsonSubTypes.Type(value = InfoLevelExportDTO.class, name = "InfoLevelImportDTO")})
@ApiModel(value = "AbstractLevelExportDTO", subTypes = {GameLevelExportDTO.class, InfoLevelExportDTO.class, AssessmentLevelExportDTO.class},
        description = "Superclass for classes GameLevelExportDTO, InfoLevelExportDTO and AssessmentLevelExportDTO")
public class AbstractLevelExportDTO {

    @ApiModelProperty(value = "Short textual description of the level.", example = "Game Level1")
    protected String title;
    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
    protected int maxScore;
    @ApiModelProperty(value = "Type of the level.", example = "GAME")
    protected LevelType levelType;
    @ApiModelProperty(value = "Order of level, starts with 0", example = "2")
    protected int order;
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", example = "5")
    protected int estimatedDuration;

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

    @Override public String toString() {
        return "AbstractLevelExportDTO{" + "title='" + title + '\'' + ", maxScore=" + maxScore + ", levelType=" + levelType + ", order="
            + order + ", estimatedDuration=" + estimatedDuration + '}';
    }
}
