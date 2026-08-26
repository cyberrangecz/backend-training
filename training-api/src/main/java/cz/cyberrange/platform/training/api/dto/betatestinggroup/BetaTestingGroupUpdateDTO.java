package cz.cyberrange.platform.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Data;

/** Encapsulates information needed for update of Beta testing group */
@Data
@ApiModel(
    value = "BetaTestingGroupUpdateDTO",
    description = "BetaTesting group to update. (Deprecated)")
public class BetaTestingGroupUpdateDTO {

  /** Carries each organizer's {@code userRefId}, not the local primary key. */
  @ApiModelProperty(
      value = "Logins of users who is allowed to see training definition.",
      required = true)
  @NotNull(message = "{betaTestingGroup.organizersRefIds.NotNull.message}")
  private Set<Long> organizersRefIds;
}
