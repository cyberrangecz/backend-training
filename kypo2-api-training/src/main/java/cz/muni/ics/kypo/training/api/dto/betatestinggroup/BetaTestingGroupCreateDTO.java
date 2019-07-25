package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Encapsulates information needed for creation of new Beta testing group.
 */
public class BetaTestingGroupCreateDTO {

    @NotNull(message = "{betaTestingGroup.organizersRefIds.NotNull.message}")
    private Set<Long> organizersRefIds;

    /**
     * Gets organizers ref ids.
     *
     * @return the organizers ref ids
     */
    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true)
    public Set<Long> getOrganizersRefIds() {
        return organizersRefIds;
    }

    /**
     * Sets organizers ref ids.
     *
     * @param organizersRefIds the organizers ref ids
     */
    public void setOrganizersRefIds(Set<Long> organizersRefIds) {
        this.organizersRefIds = organizersRefIds;
    }

    @Override
    public String toString() {
        return "BetaTestingGroupUpdateDTO{" +
                ", organizersRefIds=" + organizersRefIds +
                '}';
    }
}
