package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

/**
 * Encapsulates information about Beta testing group.
 */
@ApiModel(value = "BetaTestingGroupDTO", description = "Group of organizers who are allowed to see the specific training definitions. (Deprecated)")
public class BetaTestingGroupDTO {

    @ApiModelProperty(value = "Main identifier of beta testing group.", required = true, example = "1")
    private Long id;
    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true)
    private Set<Long> organizersRefIds;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets ref IDs of beta testers of the training definition.
     *
     * @return list of IDs
     */
    public Set<Long> getOrganizersRefIds() {
        return organizersRefIds;
    }

    /**
     * Sets ref IDs of beta testers of the training definition.
     *
     * @param organizersRefIds list of IDs
     */
    public void setOrganizersRefIds(Set<Long> organizersRefIds) {
        this.organizersRefIds = organizersRefIds;
    }

    @Override
    public String toString() {
        return "BetaTestingGroupDTO{" +
                "id=" + id +
                ", organizersRefIds=" + organizersRefIds +
                '}';
    }
}
