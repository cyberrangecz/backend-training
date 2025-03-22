package cz.cyberrange.platform.training.api.dto.visualization;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@ApiModel(value = "JeopardySublevelVisualizationDTO", description = "Information about jeopardy sublevel needed for visualizations.", parent = TrainingLevelVisualizationDTO.class)
public class JeopardySublevelVisualizationDTO extends TrainingLevelVisualizationDTO {

    @ApiModelProperty(value = "Short introductory summary of the task", example = "Exploiting insecure endpoint")
    private String description;
}
