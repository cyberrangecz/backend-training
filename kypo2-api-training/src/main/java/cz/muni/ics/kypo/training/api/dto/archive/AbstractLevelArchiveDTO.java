package cz.muni.ics.kypo.training.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonSubTypes({
        @JsonSubTypes.Type(value = GameLevelArchiveDTO.class, name = "GameLevelArchiveDTO"),
        @JsonSubTypes.Type(value = AssessmentLevelArchiveDTO.class, name = "AssessmentLevelArchiveDTO"),
        @JsonSubTypes.Type(value = InfoLevelArchiveDTO.class, name = "InfoLevelArchiveDTO")})
@ApiModel(value = "AbstractLevelArchiveDTO", subTypes = {GameLevelArchiveDTO.class, InfoLevelArchiveDTO.class, AssessmentLevelArchiveDTO.class},
        description = "Superclass for classes GameLevelArchiveDTO, InfoLevelArchiveDTO and AssessmentLevelArchiveDTO")
public class AbstractLevelArchiveDTO {

    @ApiModelProperty(value = "Main identifier of level.", example = "1")
    protected Long id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

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
