package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Set;

public class BetaTestingGroupUpdateDTO {

    @NotNull(message = "{betaTestingGroup.organizers.NotNull.message}")
    private Set<UserInfoDTO> organizers;

    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true)
    public Set<UserInfoDTO> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(Set<UserInfoDTO> organizers) {
        this.organizers = organizers;
    }

    @Override
    public String toString() {
        return "BetaTestingGroupUpdateDTO{" +
                ", organizers=" + organizers +
                '}';
    }
}
