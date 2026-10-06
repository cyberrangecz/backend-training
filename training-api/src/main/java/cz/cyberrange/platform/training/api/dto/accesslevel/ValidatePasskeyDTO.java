package cz.cyberrange.platform.training.api.dto.accesslevel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/** A participant's submission attempting to complete the access level they are currently running */
@Data
@Schema(description = "The passkey a participant submits to complete the access level they run.")
public class ValidatePasskeyDTO {

  /** The value compared against the current access level's stored passkey */
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "passkey")
  @NotEmpty(message = "{passkeyToValidate.passkey.NotEmpty.message}")
  private String passkey;
}
