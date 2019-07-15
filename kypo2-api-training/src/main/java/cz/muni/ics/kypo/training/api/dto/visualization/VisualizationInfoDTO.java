package cz.muni.ics.kypo.training.api.dto.visualization;

import java.util.List;
import java.util.Objects;

public class VisualizationInfoDTO {

    private Long trainingDefinitionId;
    private String trainingDefinitionTitle;
    private long trainingDefinitionEstimatedDuration;
    private List<AbstractLevelVisualizationDTO> levels;

    public VisualizationInfoDTO(Long trainingDefinitionId, String trainingDefinitionTitle, long trainingDefinitionEstimatedDuration, List<AbstractLevelVisualizationDTO> levels) {
        this.trainingDefinitionId = trainingDefinitionId;
        this.trainingDefinitionTitle = trainingDefinitionTitle;
        this.trainingDefinitionEstimatedDuration = trainingDefinitionEstimatedDuration;
        this.levels = levels;
    }

    /**
     * Gets id of training definition.
     *
     * @return the id
     */
    public Long getTrainingDefinitionId() {
        return trainingDefinitionId;
    }

    /**
     * Sets id of training definition.
     *
     * @param trainingDefinitionId the id of training definition.
     */
    public void setTrainingDefinitionId(Long trainingDefinitionId) {
        this.trainingDefinitionId = trainingDefinitionId;
    }

    /**
     * Gets title of training definition.
     *
     * @return the title
     */
    public String getTrainingDefinitionTitle() {
        return trainingDefinitionTitle;
    }

    /**
     * Sets title of training definition.
     *
     * @param trainingDefinitionTitle title of training definition.
     */
    public void setTrainingDefinitionTitle(String trainingDefinitionTitle) {
        this.trainingDefinitionTitle = trainingDefinitionTitle;
    }

    /**
     * Gets estimated duration of training definition.
     *
     * @return the estimated duration
     */
    public long getTrainingDefinitionEstimatedDuration() {
        return trainingDefinitionEstimatedDuration;
    }
    /**
     * Sets estimated duration of training definition.
     *
     * @param trainingDefinitionEstimatedDuration the estimated duration of training definition.
     */
    public void setTrainingDefinitionEstimatedDuration(long trainingDefinitionEstimatedDuration) {
        this.trainingDefinitionEstimatedDuration = trainingDefinitionEstimatedDuration;
    }

    /**
     * Gets list of levels of training definition.
     *
     * @return the id
     */
    public List<AbstractLevelVisualizationDTO> getLevels() {
        return levels;
    }

    /**
     * Sets list of levels of training definition.
     *
     * @param levels the levels of training definition.
     */
    public void setLevels(List<AbstractLevelVisualizationDTO> levels) {
        this.levels = levels;
    }


    @Override
    public boolean equals(Object object) {
        if (!(object instanceof VisualizationInfoDTO)) return false;
        VisualizationInfoDTO that = (VisualizationInfoDTO) object;
        return getTrainingDefinitionEstimatedDuration() == that.getTrainingDefinitionEstimatedDuration() &&
                Objects.equals(getTrainingDefinitionId(), that.getTrainingDefinitionId()) &&
                Objects.equals(getTrainingDefinitionTitle(), that.getTrainingDefinitionTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTrainingDefinitionId(), getTrainingDefinitionTitle(), getTrainingDefinitionEstimatedDuration());
    }
}
