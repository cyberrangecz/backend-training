package cz.cyberrange.platform.training.api.dto.technique;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ApiModel(value = "MitreTechniqueDTO", description = "Represent 'how' an trainee achieves a tactical goal of the training level by performing an action.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MitreTechniqueDTO {

    @ApiModelProperty(value = "Main identifier of Mitre technique.", required = true, example = "1")
    private Long id;
    @ApiModelProperty(example = "T1548.001")
    @NotEmpty(message = "{mitreTechnique.techniqueKey.NotEmpty.message}")
    private String techniqueKey;
}
