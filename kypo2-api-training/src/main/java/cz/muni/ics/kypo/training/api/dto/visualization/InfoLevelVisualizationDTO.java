package cz.muni.ics.kypo.training.api.dto.visualization;

import cz.muni.ics.kypo.training.api.enums.LevelType;

public class InfoLevelVisualizationDTO extends AbstractLevelVisualizationDTO {

    public InfoLevelVisualizationDTO() {
    }

    @Override
    public String toString() {
        return "InfoLevelVisualizationDTO{" +
                "id=" + this.getId() +
                ", title='" + this.getTitle() + '\'' +
                ", levelType=" + this.getLevelType() +
                ", order=" + this.getOrder() +
                ", maxScore=" + this.getMaxScore() +
                ", estimatedDuration=" + this.getEstimatedDuration() +
                "}";
    }
}
