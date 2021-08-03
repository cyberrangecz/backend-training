package cz.muni.ics.kypo.training.api.dto.visualization.progress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(value = "VisualizationProgressDTO", description = "A model includes necessary information about training instance which is needed for visualizations.")
public class VisualizationProgressDTO {

    @ApiModelProperty(value = "Start time of the training run.", example = "1")
    private long startTime;
    @ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.")
    private long estimatedEndTime;
    @ApiModelProperty(value = "A current time of the training.")
    private long currentTime;
    @ApiModelProperty(value = "Information about all players in training instance.")
    private List<UserRefDTO> players;
    @ApiModelProperty(value = "Information about all levels in training definition.")
    @JsonIgnoreProperties({"snapshot_hook", "training_definition", "hints.hint_penalty", "incorrect_answer_limit"})
    private List<LevelDefinitionProgressDTO> levels;
    @ApiModelProperty(value = "Information about all levels in training definition.")
    private List<PlayerProgress> playerProgress;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEstimatedEndTime() {
        return estimatedEndTime;
    }

    public void setEstimatedEndTime(long estimatedEndTime) {
        this.estimatedEndTime = estimatedEndTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public List<UserRefDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<UserRefDTO> players) {
        this.players = players;
    }

    public List<LevelDefinitionProgressDTO> getLevels() {
        return levels;
    }

    public void setLevels(List<LevelDefinitionProgressDTO> levels) {
        this.levels = levels;
    }

    public List<PlayerProgress> getPlayerProgress() {
        return playerProgress;
    }

    public void setPlayerProgress(List<PlayerProgress> playerProgress) {
        this.playerProgress = playerProgress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisualizationProgressDTO that = (VisualizationProgressDTO) o;
        return getStartTime() == that.getStartTime() &&
                getEstimatedEndTime() == that.getEstimatedEndTime() &&
                getCurrentTime() == that.getCurrentTime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStartTime(), getEstimatedEndTime(), getCurrentTime());
    }

    @Override
    public String toString() {
        return "VisualizationProgressDTO{" +
                "startTime=" + startTime +
                ", estimatedEndTime=" + estimatedEndTime +
                ", currentTime=" + currentTime +
                ", players=" + players +
                ", levels=" + levels +
                '}';
    }
}
