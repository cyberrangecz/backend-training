package cz.cyberrange.platform.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Data;

/** Encapsulates information needed for creation of new Beta testing group. */
@Data
@ApiModel(
    value = "BetaTestingGroupCreateDTO",
    description = "BetaTestingGroup to create. (Deprecated)")
public class BetaTestingGroupCreateDTO {

  @ApiModelProperty(
      value = "Logins of users who is allowed to see training definition.",
      required = true)
  @NotNull(message = "{betaTestingGroup.organizersRefIds.NotNull.message}")
  private Set<Long> organizersRefIds;
}
