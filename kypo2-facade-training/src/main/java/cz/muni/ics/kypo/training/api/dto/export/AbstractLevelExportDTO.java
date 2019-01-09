package cz.muni.ics.kypo.training.api.dto.export;

import cz.muni.ics.kypo.training.persistence.model.enums.LevelType;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda
 */
public class AbstractLevelExportDTO {

    protected String title;
    protected int maxScore;
    protected Long nextLevel;
    protected LevelType levelType;

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

    @ApiModelProperty(value = "Reference to the next abstract level (if it is null, then it is the last level)", example = "2")
    public Long getNextLevel() {
        return nextLevel;
    }

    public void setNextLevel(Long nextLevel) {
        this.nextLevel = nextLevel;
    }

    @ApiModelProperty(value = "Type of the level.", example = "GAME")
    public LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    @Override
    public String toString() {
        return "AbstractLevelExportDTO{" +
                "title='" + title + '\'' +
                ", maxScore=" + maxScore +
                ", nextLevel=" + nextLevel +
                ", levelType=" + levelType +
                '}';
    }
}
