package cz.muni.ics.kypo.training.api.dto.visualization.timeline;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TimelineDTO {

    private long estimatedTime;
    private List<Integer> maxScoreOfLevels = new ArrayList<>();
    private long maxParticipantTime;
    private float averageTime;
    private List<PlayerDataDTO> playerData = new ArrayList<>();

    public long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public List<Integer> getMaxScoreOfLevels() {
        return maxScoreOfLevels;
    }

    public void setMaxScoreOfLevels(List<Integer> maxScoreOfLevels) {
        this.maxScoreOfLevels = maxScoreOfLevels;
    }

    public long getMaxParticipantTime() {
        return maxParticipantTime;
    }

    public void setMaxParticipantTime(long maxParticipantTime) {
        this.maxParticipantTime = maxParticipantTime;
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
                ", maxScoreOfLevels=" + maxScoreOfLevels +
                ", maxParticipantTime=" + maxParticipantTime +
                ", averageTime=" + averageTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimelineDTO that = (TimelineDTO) o;
        return getEstimatedTime() == that.getEstimatedTime() &&
                getMaxParticipantTime() == that.getMaxParticipantTime() &&
                getAverageTime() == that.getAverageTime() &&
                Objects.equals(getMaxScoreOfLevels(), that.getMaxScoreOfLevels());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEstimatedTime(), getMaxScoreOfLevels(), getMaxParticipantTime(), getAverageTime());
    }
}
