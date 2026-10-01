package cz.cyberrange.platform.training.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Wraps the access token of a training instance so it is served as a JSON object */
@Data
@AllArgsConstructor
@Schema(description = "The access token of a training instance.")
public class AccessTokenDTO {

  @Schema(example = "pass-1234")
  private String accessToken;
}
