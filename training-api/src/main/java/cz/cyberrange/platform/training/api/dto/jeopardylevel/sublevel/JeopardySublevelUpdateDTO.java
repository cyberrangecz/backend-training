package cz.cyberrange.platform.training.api.dto.jeopardylevel.sublevel;

import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Getter
@Setter
@ApiModel(value = "JeopardySublevelUpdateDTO", description = "A sublevel inside Jeopardy level to update.", parent = TrainingLevelUpdateDTO.class)
public class JeopardySublevelUpdateDTO extends TrainingLevelUpdateDTO {

    @Size(min = 1, max = 100, message = "{jeopardysublevel.description.Size.message}")
    @NotNull(message = "{jeopardysublevel.description.NotNull.message}")
    @ApiModelProperty(value = "Short introductory summary of the task", example = "Exploiting insecure endpoint")
    private String description;

    public JeopardySublevelUpdateDTO() {
        this.levelType = LevelType.JEOPARDY_SUBLEVEL;
    }
}
