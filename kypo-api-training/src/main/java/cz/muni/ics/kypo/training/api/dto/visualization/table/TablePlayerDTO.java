package cz.muni.ics.kypo.training.api.dto.visualization.table;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataWithScoreDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;

import java.util.ArrayList;
import java.util.List;

public class TablePlayerDTO extends PlayerDataWithScoreDTO {

    private Boolean finished;
    private List<VisualizationAbstractLevelDTO> levels = new ArrayList<>();

    public TablePlayerDTO(Long id, String name, byte[] picture, Long trainingRunId) {
        super(id, name, picture, trainingRunId);
    }

    public TablePlayerDTO(UserRefDTO userRef, Long trainingRunId) {
        super(userRef.getUserRefId(), userRef.getUserRefFullName(), userRef.getPicture(), trainingRunId);
    }

    public TablePlayerDTO(Long id, String name, byte[] picture, Long trainingRunId,
                          Integer gameScore, Integer assessmentScore) {
        super(id, name, picture, trainingRunId, gameScore, assessmentScore);
    }

    public TablePlayerDTO(UserRefDTO userRef, Long trainingRunId, Integer gameScore, Integer assessmentScore) {
        super(userRef.getUserRefId(), userRef.getUserRefFullName(), userRef.getPicture(), trainingRunId, gameScore, assessmentScore);
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
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
        return "TablePlayerDTO{" +
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
