package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameResultsDTO {

    private long estimatedTime;
    private int maxParticipantScore;
    private int maxParticipantGameScore;
    private int maxParticipantAssessmentScore;
    private long maxParticipantTime;
    private float averageTime;
    private float averageScore;
    private float averageGameScore;
    private float averageAssessmentScore;
    private List<PlayerDataDTO> playerData = new ArrayList<>();

    public long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public int getMaxParticipantScore() {
        return maxParticipantScore;
    }

    public void setMaxParticipantScore(int maxParticipantScore) {
        this.maxParticipantScore = maxParticipantScore;
    }

    public int getMaxParticipantGameScore() {
        return maxParticipantGameScore;
    }

    public void setMaxParticipantGameScore(int maxParticipantGameScore) {
        this.maxParticipantGameScore = maxParticipantGameScore;
    }

    public int getMaxParticipantAssessmentScore() {
        return maxParticipantAssessmentScore;
    }

    public void setMaxParticipantAssessmentScore(int maxParticipantAssessmentScore) {
        this.maxParticipantAssessmentScore = maxParticipantAssessmentScore;
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

    public float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    public float getAverageGameScore() {
        return averageGameScore;
    }

    public void setAverageGameScore(float averageGameScore) {
        this.averageGameScore = averageGameScore;
    }

    public float getAverageAssessmentScore() {
        return averageAssessmentScore;
    }

    public void setAverageAssessmentScore(float averageAssessmentScore) {
        this.averageAssessmentScore = averageAssessmentScore;
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
                getMaxParticipantScore() == that.getMaxParticipantScore() &&
                getMaxParticipantGameScore() == that.getMaxParticipantGameScore() &&
                getMaxParticipantAssessmentScore() == that.getMaxParticipantAssessmentScore() &&
                getMaxParticipantTime() == that.getMaxParticipantTime() &&
                getAverageTime() == that.getAverageTime() &&
                getAverageGameScore() == that.getAverageGameScore() &&
                getAverageAssessmentScore() == that.getAverageAssessmentScore();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEstimatedTime(), getMaxParticipantScore(), getMaxParticipantGameScore(), getMaxParticipantAssessmentScore(), getMaxParticipantTime(),
                getAverageTime(), getAverageGameScore(), getAverageAssessmentScore());
    }


    @Override
    public String toString() {
        return "GameResultsDTO{" +
                "estimatedTime=" + estimatedTime +
                ", maxParticipantScore=" + maxParticipantScore +
                ", maxParticipantGameScore=" + maxParticipantGameScore +
                ", maxParticipantAssessmentScore=" + maxParticipantAssessmentScore +
                ", maxParticipantTime=" + maxParticipantTime +
                ", averageTime=" + averageTime +
                ", averageScore=" + averageScore +
                ", averageGameScore=" + averageGameScore +
                ", averageAssessmentScore=" + averageAssessmentScore +
                '}';
    }
}
