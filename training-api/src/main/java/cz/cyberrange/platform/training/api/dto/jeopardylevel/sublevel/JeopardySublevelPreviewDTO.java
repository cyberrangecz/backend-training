package cz.cyberrange.platform.training.api.dto.jeopardylevel.sublevel;

import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelPreviewDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@ApiModel(value = "JeopardySublevelViewDTO", description = "Preview of a Jeopardy level.", parent = TrainingLevelPreviewDTO.class)
public class JeopardySublevelPreviewDTO extends TrainingLevelPreviewDTO {

    @ApiModelProperty(value = "Short introductory summary of the task", example = "Exploiting insecure endpoint")
    private String description;
}
