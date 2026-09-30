package cz.cyberrange.platform.training.api.dto.betatestinggroup;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;

/**
 * The group of users who may see and run a training definition while it is still unreleased, as it
 * leaves the service
 */
@Data
@Schema(description = "The users allowed to see a training definition before it is released.")
public class BetaTestingGroupDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  private Long id;

  /**
   * Carries each member's {@code userRefId}, the id spoken outside this service, rather than the
   * local primary key of the user row. Empty rather than null when the group has no members.
   */
  @Schema(
      description = "Ids of the members as the user and group service numbers them.",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Set<Long> organizersRefIds;
}
