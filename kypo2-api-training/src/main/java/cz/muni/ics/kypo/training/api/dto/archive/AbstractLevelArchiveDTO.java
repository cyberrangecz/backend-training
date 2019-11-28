package cz.muni.ics.kypo.training.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about abstract level.
 * Used for archiving.
 * Extended by {@link AssessmentLevelArchiveDTO}, {@link GameLevelArchiveDTO} and {@link InfoLevelArchiveDTO}.
 */
@JsonSubTypes({
        @JsonSubTypes.Type(value = GameLevelArchiveDTO.class, name = "GameLevelArchiveDTO"),
        @JsonSubTypes.Type(value = AssessmentLevelArchiveDTO.class, name = "AssessmentLevelArchiveDTO"),
        @JsonSubTypes.Type(value = InfoLevelArchiveDTO.class, name = "InfoLevelArchiveDTO")})
@ApiModel(value = "AbstractLevelArchiveDTO", subTypes = {GameLevelArchiveDTO.class, InfoLevelArchiveDTO.class, AssessmentLevelArchiveDTO.class},
        description = "Superclass for classes GameLevelArchiveDTO, InfoLevelArchiveDTO and AssessmentLevelArchiveDTO")
public class AbstractLevelArchiveDTO {

    /**
     * The Id.
     */
    @ApiModelProperty(value = "Main identifier of level.", example = "1")
    protected Long id;
    /**
     * The Title.
     */
    @ApiModelProperty(value = "Short textual description of the level.", example = "Game Level1")
    protected String title;
    /**
     * The Max score.
     */
    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
    protected int maxScore;
    /**
     * The Level type.
     */
    @ApiModelProperty(value = "Type of the level.", example = "GAME")
    protected LevelType levelType;
    /**
     * The Order.
     */
    @ApiModelProperty(value = "Order of level, starts with 0", example = "2")
    protected int order;
    /**
     * The Estimated duration.
     */
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", example = "5")
    protected int estimatedDuration;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
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
     * @return the level type
     */
    public LevelType getLevelType() {
        return levelType;
    }

    /**
     * Sets level type.
     *
     * @param levelType the level type
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

    @Override
    public String toString() {
        return "AbstractLevelArchiveDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", maxScore=" + maxScore +
                ", levelType=" + levelType +
                ", order=" + order +
                ", estimatedDuration=" + estimatedDuration +
                '}';
    }
}
