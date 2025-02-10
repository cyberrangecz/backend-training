package cz.cyberrange.platform.training.api.dto.visualization;

import cz.cyberrange.platform.training.api.dto.imports.AbstractLevelImportDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@ApiModel(value = "VisualizationInfoDTO", description = "A model includes necessary information about training definition which is needed for visualizations.", parent = AbstractLevelImportDTO.class)
public class VisualizationInfoDTO {
    @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
    private Long trainingDefinitionId;
    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
    private String trainingDefinitionTitle;
    @ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.", example = "5")
    private long trainingDefinitionEstimatedDuration;
    @ApiModelProperty(value = "Information about all levels in training definition.")
    private List<AbstractLevelVisualizationDTO> levels;
}
