package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;

import java.util.Objects;

import cz.muni.ics.kypo.training.api.dto.snapshothook.SnapshotHookDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "AbstractLevelDTO", subTypes = {GameLevelDTO.class, InfoLevelDTO.class, AssessmentLevelDTO.class},
        description = "Superclass for classes GameLevelDTO, AssessmentLevelDTO and InfoLevelDTO")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GameLevelDTO.class, name = "GameLevelDTO"),
        @JsonSubTypes.Type(value = AssessmentLevelDTO.class, name = "AssessmentLevelDTO"),
        @JsonSubTypes.Type(value = InfoLevelDTO.class, name = "InfoLevelDTO")})
public class AbstractLevelDTO {

    protected Long id;
    protected String title;
    protected int maxScore;
    protected Long nextLevel;
    protected SnapshotHookDTO snapshotHook;
    protected LevelType levelType;
    protected int estimatedDuration;

    @ApiModelProperty(value = "Main identifier of level.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public SnapshotHookDTO getSnapshotHook() {
        return snapshotHook;
    }

    public void setSnapshotHook(SnapshotHookDTO snapshotHook) {
        this.snapshotHook = snapshotHook;
    }

    @ApiModelProperty(value = "Type of the level.", example = "GAME")
    public LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    @ApiModelProperty(value = "Estimated time taken by the player to resolve the level.", example = "5")
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AbstractLevelDTO))
            return false;
        AbstractLevelDTO other = (AbstractLevelDTO) obj;
        return Objects.equals(id, other.getId());
    }

    @Override public String toString() {
        return "AbstractLevelDTO{" + "id=" + id + ", title='" + title + '\'' + ", maxScore=" + maxScore + ", nextLevel=" + nextLevel
            + ", snapshotHook=" + snapshotHook + ", levelType=" + levelType + ", estimatedDuration=" + estimatedDuration + '}';
    }
}

