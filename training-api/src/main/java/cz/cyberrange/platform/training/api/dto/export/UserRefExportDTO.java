package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Encapsulates information about user reference */
@Data
@Schema(description = "The trainee an exported training run belongs to")
public class UserRefExportDTO {

  @Schema(description = "Left unset: no source field feeds it on export")
  private String userRefLogin;

  @Schema(example = "Mgr. John Doe")
  private String userRefFullName;

  @Schema(example = "John")
  private String userRefGivenName;

  @Schema(example = "Doe")
  private String userRefFamilyName;

  @Schema(
      description = "Issuer of the identity the user signed in with",
      example = "https://oidc.provider.cz")
  private String iss;

  @Schema(
      description = "Id of the user in the user and group service, the one used across services",
      example = "1")
  private Long userRefId;
}
