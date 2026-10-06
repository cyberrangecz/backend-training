package cz.cyberrange.platform.training.api.dto.betatestinggroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

/**
 * The membership submitted for the group that may see a training definition while it is still
 * unreleased, carried as part of a definition being created
 */
@Data
@Schema(description = "The organizers to put in a new definition's beta testing group.")
public class BetaTestingGroupCreateDTO {

  /** Carries each organizer's {@code userRefId}, not the local primary key */
  @Schema(
      description = "Ids of the organizers as the user and group service numbers them.",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "{betaTestingGroup.organizersRefIds.NotNull.message}")
  private Set<Long> organizersRefIds;
}
