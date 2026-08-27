package cz.cyberrange.platform.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import lombok.Data;

/**
 * The group of users who may see and run a training definition while it is still unreleased, as it
 * leaves the service.
 */
@Data
@ApiModel(
    value = "BetaTestingGroupDTO",
    description =
        "Group of organizers who are allowed to see the specific training definitions. (Deprecated)")
public class BetaTestingGroupDTO {

  @ApiModelProperty(
      value = "Main identifier of beta testing group.",
      required = true,
      example = "1")
  private Long id;

  /**
   * Carries each member's {@code userRefId}, the id spoken outside this service, rather than the
   * local primary key of the user row. Empty rather than null when the group has no members.
   */
  @ApiModelProperty(
      value = "Logins of users who is allowed to see training definition.",
      required = true)
  private Set<Long> organizersRefIds;
}
