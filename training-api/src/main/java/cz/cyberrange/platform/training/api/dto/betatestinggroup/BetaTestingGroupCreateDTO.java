package cz.cyberrange.platform.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * The membership submitted for the group that may see a training definition while it is still
 * unreleased, carried as part of a definition being created.
 */
@Data
@ApiModel(
    value = "BetaTestingGroupCreateDTO",
    description = "BetaTestingGroup to create. (Deprecated)")
public class BetaTestingGroupCreateDTO {

  /** Carries each organizer's {@code userRefId}, not the local primary key. */
  @ApiModelProperty(
      value = "Logins of users who is allowed to see training definition.",
      required = true)
  @NotNull(message = "{betaTestingGroup.organizersRefIds.NotNull.message}")
  private Set<Long> organizersRefIds;
}
