package cz.muni.ics.kypo.training.api.dto.visualization;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about abstract level.
 * Used for visualization.
 * Extended by {@link AssessmentLevelVisualizationDTO}, {@link GameLevelVisualizationDTO} and {@link InfoLevelVisualizationDTO}.
 */
@ApiModel(value = "AbstractLevelVisualizationDTO", subTypes = {GameLevelVisualizationDTO.class, InfoLevelVisualizationDTO.class, AssessmentLevelVisualizationDTO.class},
        description = "Superclass for classes GameLevelDTO, AssessmentLevelDTO and InfoLevelDTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractLevelVisualizationDTO {

    @ApiModelProperty(value = "Main identifier of level.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Short textual description of the level.", example = "Game Level1")
    private String title;
    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
    private int maxScore;
    @ApiModelProperty(value = "Type of the level.", example = "GAME")
    private LevelType levelType;
    @ApiModelProperty(value = "Estimated time taken by the player to resolve the level.", example = "5")
    private long estimatedDuration;
    @ApiModelProperty(value = "Order of level among levels in training definition.", example = "1")
    private int order;

    /**
     * Instantiates a new Visualization level info dto.
     */
    public AbstractLevelVisualizationDTO() {
    }

    /**
     * Instantiates a new Visualization level info dto.
     *
     * @param id                the id
     * @param title             the title
     * @param levelType         the level type
     * @param order             the order
     * @param maxScore          the maximum score
     * @param estimatedDuration the estimated duration
     */
    public AbstractLevelVisualizationDTO(Long id, String title, LevelType levelType, int order, int maxScore, long estimatedDuration) {
        this.id = id;
        this.title = title;
        this.levelType = levelType;
        this.order = order;
        this.maxScore = maxScore;
        this.estimatedDuration = estimatedDuration;
    }

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
     * Gets order number of level that is compared with order numbers of other levels associated with same definition.
     * First level from definition has order of 0
     *
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets order number of level that is compared with order numbers of other levels associated with same definition.
     * First level from definition has order of 0
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
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
     * Gets maximum score of level.
     *
     * @return the maximum score of this level which can trainee achieve.
     */
    public int getMaxScore() {
        return maxScore;
    }

    /**
     * Sets maximum score of level.
     *
     * @param maxScore the maximum score of this level.
     */
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    /**
     * Gets estimated time in minutes that it takes to solve level
     *
     * @return the estimated duration
     */
    public long getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Sets estimated time in minutes that it takes to solve level
     *
     * @param estimatedDuration the estimated duration
     */
    public void setEstimatedDuration(long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AbstractLevelVisualizationDTO)) return false;
        AbstractLevelVisualizationDTO that = (AbstractLevelVisualizationDTO) object;
        return getOrder() == that.getOrder() &&
                getMaxScore() == that.getMaxScore() &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                getLevelType() == that.getLevelType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getLevelType(), getOrder(), getMaxScore());
    }

    @Override
    public String toString() {
        return "AbstractLevelVisualizationDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", levelType=" + levelType +
                ", order=" + order +
                ", maxScore=" + maxScore +
                '}';
    }
}
