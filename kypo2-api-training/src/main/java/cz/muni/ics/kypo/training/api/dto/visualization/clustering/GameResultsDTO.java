package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameResultsDTO {

    private long estimatedTime;
    private int maxPoints;
    private long maxTime;
    private float averageTime;
    private float averageScore;
    private List<PlayerDataDTO> playerData = new ArrayList<>();

    public long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
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

    public float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    public List<PlayerDataDTO> getPlayerData() {
        return playerData;
    }

    public void addPlayerData(PlayerDataDTO playerDataDTO) {
        this.playerData.add(playerDataDTO);
    }

    public void setPlayerData(List<PlayerDataDTO> playerData) {
        this.playerData = playerData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameResultsDTO that = (GameResultsDTO) o;
        return getEstimatedTime() == that.getEstimatedTime() &&
                getMaxPoints() == that.getMaxPoints() &&
                getMaxTime() == that.getMaxTime() &&
                getAverageTime() == that.getAverageTime() &&
                getAverageScore() == that.getAverageScore();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEstimatedTime(), getMaxPoints(), getMaxTime(), getAverageTime(), getAverageScore());
    }

    @Override
    public String toString() {
        return "GameResultsDTO{" +
                "estimatedTime=" + estimatedTime +
                ", maxPoints=" + maxPoints +
                ", maxTime=" + maxTime +
                ", averageTime=" + averageTime +
                ", averageScore=" + averageScore +
                '}';
    }
}
