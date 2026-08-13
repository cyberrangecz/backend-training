package cz.cyberrange.platform.training.api.dto.accesslevel;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ValidatePasskeyDTO {

  @ApiModelProperty(value = "Passkey to be validated.", required = true, example = "passkey")
  @NotEmpty(message = "{passkeyToValidate.passkey.NotEmpty.message}")
  private String passkey;
}
