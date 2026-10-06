package cz.cyberrange.platform.training.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Wraps the verdict on a submitted passkey so it is served as a JSON object */
@Data
@AllArgsConstructor
@Schema(description = "The verdict on a submitted passkey.")
public class IsCorrectPasskeyDTO {

  @Schema(example = "true")
  private boolean correct;
}
