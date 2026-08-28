package cz.cyberrange.platform.training.api.dto.accesslevel;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/** A participant's submission attempting to complete the access level they are currently running */
@Data
public class ValidatePasskeyDTO {

  /** The value compared against the current access level's stored passkey */
  @ApiModelProperty(value = "Passkey to be validated.", required = true, example = "passkey")
  @NotEmpty(message = "{passkeyToValidate.passkey.NotEmpty.message}")
  private String passkey;
}
