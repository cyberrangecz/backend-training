package cz.cyberrange.platform.training.api.dto.betatestinggroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

/**
 * The membership submitted for the group that may see a training definition while it is still
 * unreleased, carried as part of a definition being updated. Submitting it replaces the existing
 * membership outright, and an empty membership is the only way to empty the group: leaving it out
 * of an update whose definition already has a group is rejected as a conflict rather than deleting
 * that group.
 */
@Data
@Schema(description = "The organizers that replace the beta testing group's membership.")
public class BetaTestingGroupUpdateDTO {

  /** Carries each organizer's {@code userRefId}, not the local primary key */
  @Schema(
      description = "Ids of the organizers as the user and group service numbers them.",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "{betaTestingGroup.organizersRefIds.NotNull.message}")
  private Set<Long> organizersRefIds;
}
