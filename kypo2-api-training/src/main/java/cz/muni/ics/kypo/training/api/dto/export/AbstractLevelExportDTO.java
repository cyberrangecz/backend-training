package cz.muni.ics.kypo.training.api.dto.export;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = GameLevelExportDTO.class, name = "GameLevelImportDTO"),
    @JsonSubTypes.Type(value = AssessmentLevelExportDTO.class, name = "AssessmentLevelImportDTO"),
    @JsonSubTypes.Type(value = InfoLevelExportDTO.class, name = "InfoLevelImportDTO")})
public class AbstractLevelExportDTO {

    protected String title;
    protected int maxScore;
    protected LevelType levelType;
    protected int order;
    protected int estimatedDuration;

    public AbstractLevelExportDTO(){}

    @ApiModelProperty(value = "Short textual description of the level.", example = "Game Level1")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    @ApiModelProperty(value = "Type of the level.", example = "GAME")
    public LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    @ApiModelProperty(value = "Order of level, starts with 0", example = "2")
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @ApiModelProperty(value = "Estimated time taken by the player to resolve the level.", example = "5")
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    @Override public String toString() {
        return "AbstractLevelExportDTO{" + "title='" + title + '\'' + ", maxScore=" + maxScore + ", levelType=" + levelType + ", order="
            + order + ", estimatedDuration=" + estimatedDuration + '}';
    }
}
