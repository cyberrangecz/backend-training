package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;

import java.util.Set;

/**
 * Encapsulates information about Beta testing group.
 */
public class BetaTestingGroupDTO {

    private Long id;
    private Set<UserRefDTO> organizers;

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
     * Gets organizers.
     *
     * @return the set of {@link UserRefDTO}
     */
    public Set<UserRefDTO> getOrganizers() {
        return organizers;
    }

    /**
     * Sets organizers.
     *
     * @param organizers the set of {@link UserRefDTO}
     */
    public void setOrganizers(Set<UserRefDTO> organizers) {
        this.organizers = organizers;
    }

    @Override
    public String toString() {
        return "BetaTestingGroupDTO{" +
                "id=" + id +
                ", organizers=" + organizers +
                '}';
    }
}
