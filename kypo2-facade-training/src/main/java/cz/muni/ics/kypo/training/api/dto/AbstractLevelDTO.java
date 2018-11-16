package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.persistence.model.enums.LevelType;

import java.util.Objects;

import cz.muni.ics.kypo.training.api.dto.posthook.PostHookDTO;
import cz.muni.ics.kypo.training.api.dto.prehook.PreHookDTO;
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
    protected PreHookDTO preHook;
    protected PostHookDTO postHook;
    protected LevelType levelType;

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

    public PreHookDTO getPreHook() {
        return preHook;
    }

    public void setPreHook(PreHookDTO preHook) {
        this.preHook = preHook;
    }

    public PostHookDTO getPostHook() {
        return postHook;
    }

    public void setPostHook(PostHookDTO postHook) {
        this.postHook = postHook;
    }

    @ApiModelProperty(value = "Type of the level.", example = "GAME")
    public LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
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

    @Override
    public String toString() {
        return "AbstractLevelDTO [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", nextLevel=" + nextLevel + ", preHook="
                + preHook + ", postHook=" + postHook + "]";
    }

}

