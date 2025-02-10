package cz.cyberrange.platform.training.api.dto.accesslevel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ToString
public class ValidatePasskeyDTO {

    @ApiModelProperty(value = "Passkey to be validated.", required = true, example = "passkey")
    @NotEmpty(message = "{passkeyToValidate.passkey.NotEmpty.message}")
    private String passkey;
}
