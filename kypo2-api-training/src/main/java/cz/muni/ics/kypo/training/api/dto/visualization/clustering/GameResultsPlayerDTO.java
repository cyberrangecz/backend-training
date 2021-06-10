package cz.muni.ics.kypo.training.api.dto.visualization.clustering;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataWithScoreDTO;

public class GameResultsPlayerDTO extends PlayerDataWithScoreDTO {

    private Boolean finished;

    public GameResultsPlayerDTO(UserRefDTO userRef, Long trainingRunId, long trainingTime, Integer gameScore, Integer assessmentScore, Boolean finished) {
        super(userRef.getUserRefId(), userRef.getUserRefFullName(), userRef.getPicture(), trainingRunId, trainingTime, gameScore, assessmentScore);
        this.finished = finished;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "GameResultsPlayerDTO{" +
                "id=" + getId() +
                ", trainingRunId=" + getTrainingRunId() +
                ", name='" + getName() + '\'' +
                ", gameScore=" + getGameScore() +
                ", assessmentScore=" + getAssessmentScore() +
                ", trainingTime=" + getTrainingTime() +
                ", finished=" + finished +
                '}';
    }
}
