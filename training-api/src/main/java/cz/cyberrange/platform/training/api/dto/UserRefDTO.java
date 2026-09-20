package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Encapsulates information about user reference. Contains generally safe, descriptive-only data
 * accessible by both organizers and trainees.
 */
@Data
@Schema(
    description =
        "A platform user as this service refers to them, with the details the user and group"
            + " service holds")
public class UserRefDTO {

  /**
   * Holds the cross-service user reference id, never the local primary key. {@code UserRefMapper}
   * sets it by matching {@code UserRef.userRefId}, and {@code UserService} sets it by deserializing
   * the user-and-group service's response, aliased from that response's {@code id} or {@code
   * user_ref_id} JSON key.
   */
  @JsonAlias({"id", "user_ref_id"})
  @Schema(
      description = "The id the user is known by across the platform's services.",
      example = "1")
  private Long userRefId;

  /**
   * Set only by deserializing the user-and-group service's response, from that response's {@code
   * sub} JSON key; {@code UserRefMapper} leaves it unset, since the local {@code UserRef} entity
   * carries no matching property
   */
  @Schema(description = "The login the user signs in with.", example = "999999@mail.example.cz")
  @JsonProperty("sub")
  private String userRefSub;

  /**
   * Set only by deserializing the user-and-group service's response, from that response's {@code
   * full_name} JSON key; {@code UserRefMapper} leaves it unset, since the local {@code UserRef}
   * entity carries no matching property
   */
  @Schema(example = "Mgr. John Doe")
  @JsonProperty("full_name")
  private String userRefFullName;

  /**
   * Set only by deserializing the user-and-group service's response, from that response's {@code
   * given_name} JSON key. Nothing in this service reads it back.
   */
  @Schema(example = "John")
  @JsonProperty("given_name")
  private String userRefGivenName;

  /**
   * Set only by deserializing the user-and-group service's response, from that response's {@code
   * family_name} JSON key. Nothing in this service reads it back.
   */
  @Schema(example = "Doe")
  @JsonProperty("family_name")
  private String userRefFamilyName;

  /**
   * Set only by deserializing the user-and-group service's response, from that response's {@code
   * iss} JSON key. Nothing in this service reads it back.
   */
  @Schema(
      description = "Identity provider the user's account comes from.",
      example = "https://oidc.provider.cz")
  private String iss;

  /**
   * Set only by deserializing the user-and-group service's response. Nothing in this service reads
   * it back.
   */
  @Schema(
      description = "Profile picture of the user, base64 encoded.",
      type = "string",
      example =
          "iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAYAAAA4TnrqAAACIUlEQVR4Xu3YsY0dSQxAQQUlpXT5Z3CS/YgxSrQa4gLlEOBb9pj/x6//fv7/t/78/XhN3yBWyz3kBX2DWC33kBf0DWK13ENe0DeI1XIPeUHfIFbLPeQFfYNYLfeQF/QNYrXcQ17QN4jVcg95Qd8gVss95AV9g1gt95AX9A1itdxDXtA3iNVyD3lB3yBWyz3kBX2DWC33kBf0DWLERGOiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS6z+8b/mPha4jwXuY4H7WOA+FriPBe5jgftY4D4WuI8F7mOB+1jgPha4jwXGbzbn2xicb2Nwvo3B+TYG59sYnG9jcL6Nwfk2BufbGJxvY3C+jcH5Ngbn2xicb2Nwvq1+z2pMtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3RlvgNt34wfeJElG8AAAAASUVORK5CYII=")
  private byte[] picture;

  /**
   * Set only by deserializing the user-and-group service's response. Nothing in this service reads
   * it back.
   */
  @Schema(example = "johndoe@mail.example.cz")
  private String mail;
}
