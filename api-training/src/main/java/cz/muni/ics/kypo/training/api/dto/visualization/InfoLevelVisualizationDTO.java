package cz.muni.ics.kypo.training.api.dto.visualization;


import cz.muni.ics.kypo.training.api.dto.imports.AbstractLevelImportDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about info level. Inherits from {@link AbstractLevelVisualizationDTO}
 * Used for visualization.
 */
@ApiModel(value = "InfoLevelVisualizationDTO", description = "Information about info level needed for visualizations.", parent = AbstractLevelImportDTO.class)
public class InfoLevelVisualizationDTO extends AbstractLevelVisualizationDTO {

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Informational stuff")
    private String content;

    /**
     * Instantiates a new Info level visualization dto.
     */
    public InfoLevelVisualizationDTO() {
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
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
