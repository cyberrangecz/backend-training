package cz.muni.ics.kypo.training.api.dto.visualization.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "PlayerProgress", description = "Progress of player in Training Run.")
@JsonRootName(value = "player_progress")
public class PlayerProgress {

    @ApiModelProperty(value = "User Ref ID.", required = true)
    @JsonProperty(value = "user_ref_id", required = true)
    private long userRefId;
    @ApiModelProperty(value = "Levels ID.", required = true)
    @JsonProperty(value = "levels", required = true)
    private List<LevelProgress> levels;

    public void addLevelProgress(LevelProgress levelProgress) {
        if (levels == null) {
            levels = new ArrayList<>();
        }
        levels.add(levelProgress);
    }

    public long getUserRefId() {
        return userRefId;
    }

    public void setUserRefId(long userRefId) {
        this.userRefId = userRefId;
    }

    public List<LevelProgress> getLevels() {
        return levels;
    }

    @Override
    public String toString() {
        return "PlayerProgress{" +
                "userRefId=" + userRefId +
                ", levels=" + levels +
                '}';
    }

    public void setLevels(List<LevelProgress> levels) {
        this.levels = levels;
    }
}
