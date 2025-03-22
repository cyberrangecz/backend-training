package cz.cyberrange.platform.training.api.dto.jeopardylevel.sublevel;

import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelViewDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@ApiModel(value = "JeopardySublevelViewDTO", description = "View of a Jeopardy level.", parent = TrainingLevelViewDTO.class)
public class JeopardySublevelViewDTO extends TrainingLevelViewDTO {

    @ApiModelProperty(value = "Short introductory summary of the task", example = "Exploiting insecure endpoint")
    private String description;
}
