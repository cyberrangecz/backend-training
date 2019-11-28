package cz.muni.ics.kypo.training.api.dto.visualization;


import cz.muni.ics.kypo.training.api.dto.imports.AbstractLevelImportDTO;
import io.swagger.annotations.ApiModel;

/**
 * Encapsulates information about info level. Inherits from {@link AbstractLevelVisualizationDTO}
 * Used for visualization.
 */
@ApiModel(value = "InfoLevelVisualizationDTO", description = "Information about info level needed for visualizations.", parent = AbstractLevelImportDTO.class)
public class InfoLevelVisualizationDTO extends AbstractLevelVisualizationDTO {

    /**
     * Instantiates a new Info level visualization dto.
     */
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
