package cz.muni.ics.kypo.training.api.dto.gamelevel;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

public class ValidateFlagDTO {
    @ApiModelProperty(value = "Flag to be validated.", required = true, example = "flag")
    @NotEmpty(message = "{flagToValidate.flag.NotEmpty.message}")
    private String flag;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
