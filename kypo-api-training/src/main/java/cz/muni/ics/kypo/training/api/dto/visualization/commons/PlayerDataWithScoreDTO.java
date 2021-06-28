package cz.muni.ics.kypo.training.api.dto.visualization.commons;

public class PlayerDataWithScoreDTO extends PlayerDataDTO {

    private Integer gameScore;
    private Integer assessmentScore;

    public PlayerDataWithScoreDTO(Long id, String name, byte[] picture, Long trainingRunId) {
        super(id, name, picture, trainingRunId);
    }

    public PlayerDataWithScoreDTO(Long id, String name, byte[] picture, Long trainingRunId, Integer gameScore,
                                  Integer assessmentScore) {
        super(id, name, picture, trainingRunId);
        this.gameScore = gameScore;
        this.assessmentScore = assessmentScore;
    }

    public PlayerDataWithScoreDTO(Long id, String name, byte[] picture, Long trainingRunId,
                                  long trainingTime, Integer gameScore, Integer assessmentScore) {
        super(id, name, picture, trainingRunId, trainingTime);
        this.gameScore = gameScore;
        this.assessmentScore = assessmentScore;
    }

    public Integer getGameScore() {
        return gameScore;
    }

    public void setGameScore(Integer gameScore) {
        this.gameScore = gameScore;
    }

    public Integer getAssessmentScore() {
        return assessmentScore;
    }

    public void setAssessmentScore(Integer assessmentScore) {
        this.assessmentScore = assessmentScore;
    }

    @Override
    public String toString() {
        return "PlayerDataWithScoreDTO{" +
                "id=" + getId() +
                ", trainingRunId=" + getTrainingRunId() +
                ", name='" + getName() + '\'' +
                ", trainingTime=" + getTrainingTime() +
                ", gameScore=" + gameScore +
                ", assessmentScore=" + assessmentScore +
                '}';
    }
}
