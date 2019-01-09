package cz.muni.ics.kypo.training.api.dto.export;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Seda
 */
public class ExportTrainingDefinitionsAndLevelsDTO {

    private List<TrainingDefinitionExportDTO> trainingDefinitions = new ArrayList<>();
    private List<AbstractLevelExportDTO> levels = new ArrayList<>();

    public ExportTrainingDefinitionsAndLevelsDTO() {
    }

    public List<TrainingDefinitionExportDTO> getTrainingDefinitions() {
        return trainingDefinitions;
    }

    public void setTrainingDefinitions(List<TrainingDefinitionExportDTO> trainingDefinitions) {
        this.trainingDefinitions = trainingDefinitions;
    }

    public List<AbstractLevelExportDTO> getLevels() {
        return levels;
    }

    public void setLevels(List<AbstractLevelExportDTO> levels) {
        this.levels = levels;
    }

    @Override
    public String toString() {
        return "ExportTrainingDefinitionsAndLevelsDTO{" +
                "trainingDefinitions=" + trainingDefinitions +
                ", levels=" + levels +
                '}';
    }
}
