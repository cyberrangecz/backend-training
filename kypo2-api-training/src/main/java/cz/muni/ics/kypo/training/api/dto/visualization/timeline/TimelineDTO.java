package cz.muni.ics.kypo.training.api.dto.visualization.timeline;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TimelineDTO {

    private long estimatedTime;
    private List<Integer> levelPoints = new ArrayList<>();
    private long maxTime;
    private float averageTime;
    private List<PlayerDataDTO> playerData = new ArrayList<>();

    public long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public List<Integer> getLevelPoints() {
        return levelPoints;
    }

    public void setLevelPoints(List<Integer> levelPoints) {
        this.levelPoints = levelPoints;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public float getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(float averageTime) {
        this.averageTime = averageTime;
    }

    public List<PlayerDataDTO> getPlayerData() {
        return playerData;
    }

    public void setPlayerData(List<PlayerDataDTO> playerData) {
        this.playerData = playerData;
    }

    public void addPlayerData(PlayerDataDTO playerDataDTO) {
        this.playerData.add(playerDataDTO);
    }

    @Override
    public String toString() {
        return "TimelineDTO{" +
                "estimatedTime=" + estimatedTime +
                ", levelPoints=" + levelPoints +
                ", maxTime=" + maxTime +
                ", averageTime=" + averageTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimelineDTO that = (TimelineDTO) o;
        return getEstimatedTime() == that.getEstimatedTime() &&
                getMaxTime() == that.getMaxTime() &&
                getAverageTime() == that.getAverageTime() &&
                Objects.equals(getLevelPoints(), that.getLevelPoints());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEstimatedTime(), getLevelPoints(), getMaxTime(), getAverageTime());
    }
}
