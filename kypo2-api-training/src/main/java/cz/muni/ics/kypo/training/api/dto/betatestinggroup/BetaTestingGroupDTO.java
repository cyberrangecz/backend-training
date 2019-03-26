package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;

import java.util.Set;

public class BetaTestingGroupDTO {

    private Long id;
    private Set<UserRefDTO> organizers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<UserRefDTO> getOrganizers() {
        return organizers;
    }

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
