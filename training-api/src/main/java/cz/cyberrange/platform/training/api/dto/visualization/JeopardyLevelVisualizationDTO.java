package cz.cyberrange.platform.training.api.dto.visualization;

import cz.cyberrange.platform.training.api.dto.imports.AbstractLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.jeopardylevel.category.JeopardyCategoryDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelVisualizationDTO}
 * Used for visualization.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "JeopardyLevelVisualizationDTO", description = "Information about assessment level needed for visualizations.", parent = AbstractLevelImportDTO.class)
public class JeopardyLevelVisualizationDTO extends AbstractLevelVisualizationDTO {

    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    private AssessmentType assessmentType;
    @ApiModelProperty(value = "List of questions in this assessment as JSON.", example = "What is my mothers name?")
    private List<JeopardyCategoryDTO<JeopardySublevelVisualizationDTO>> questions;
}
