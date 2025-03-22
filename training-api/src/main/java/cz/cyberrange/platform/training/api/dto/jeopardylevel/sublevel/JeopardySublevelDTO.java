package cz.cyberrange.platform.training.api.dto.jeopardylevel.sublevel;

import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@ApiModel(value = "JeopardySublevelDTO", description = "A level inside Jeopardy level, containing task.", parent = TrainingLevelDTO.class)
public class JeopardySublevelDTO extends TrainingLevelDTO {

    @ApiModelProperty(value = "Short introductory summary of the task", example = "Exploiting insecure endpoint")
    private String description;
}
