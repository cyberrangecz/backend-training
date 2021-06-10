package cz.muni.ics.kypo.training.api.dto.visualization.timeline;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataWithScoreDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;

import java.util.ArrayList;
import java.util.List;

public class TimelinePlayerDTO extends PlayerDataWithScoreDTO {

    private List<VisualizationAbstractLevelDTO> levels = new ArrayList<>();

    public TimelinePlayerDTO(Long id, String name, byte[] picture, Long trainingRunId,
                             Integer gameScore, Integer assessmentScore) {
        super(id, name, picture, trainingRunId, gameScore, assessmentScore);
    }

    public TimelinePlayerDTO(UserRefDTO userRef, Long trainingRunId, Integer gameScore, Integer assessmentScore) {
        super(userRef.getUserRefId(), userRef.getUserRefFullName(), userRef.getPicture(), trainingRunId, gameScore, assessmentScore);
    }

    public List<VisualizationAbstractLevelDTO> getLevels() {
        return levels;
    }

    public void setLevels(List<VisualizationAbstractLevelDTO> levels) {
        this.levels = levels;
    }

    public void addTableLevel(VisualizationAbstractLevelDTO visualizationAbstractLevelDTO) {
        this.levels.add(visualizationAbstractLevelDTO);
    }

    @Override
    public String toString() {
        return "TimelinePlayerDTO{" +
                "id=" + getId() +
                ", trainingRunId=" + getTrainingRunId() +
                ", name='" + getName() + '\'' +
                ", gameScore=" + getGameScore() +
                ", assessmentScore=" + getAssessmentScore() +
                ", trainingTime=" + getTrainingTime() +
                '}';
    }


}
