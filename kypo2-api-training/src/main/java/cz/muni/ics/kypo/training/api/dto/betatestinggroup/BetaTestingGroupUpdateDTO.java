package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Encapsulates information needed for update of Beta testing group
 */
public class BetaTestingGroupUpdateDTO {

    @NotNull(message = "{betaTestingGroup.organizersRefIds.NotNull.message}")
    private Set<Long> organizersRefIds;

    /**
     * Gets organizers user ref ids.
     *
     * @return the organizers user ref ids
     */
    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true)
    public Set<Long> getOrganizersRefIds() {
        return organizersRefIds;
    }

    /**
     * Sets organizers user ref ids.
     *
     * @param organizersRefIds the organizers user ref ids
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
