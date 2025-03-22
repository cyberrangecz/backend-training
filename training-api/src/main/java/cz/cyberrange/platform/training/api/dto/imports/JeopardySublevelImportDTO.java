package cz.cyberrange.platform.training.api.dto.imports;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Getter
@Setter
@ApiModel(value = "JeopardySublevelImportDTO", description = "A sublevel inside Jeopardy level to import.", parent = TrainingLevelImportDTO.class)
public class JeopardySublevelImportDTO extends TrainingLevelImportDTO {

    @Size(min = 1, max = 100, message = "{jeopardysublevel.description.Size.message}")
    @NotNull(message = "{jeopardysublevel.description.NotNull.message}")
    @ApiModelProperty(value = "Short introductory summary of the task", example = "Exploiting insecure endpoint")
    private String description;
}
