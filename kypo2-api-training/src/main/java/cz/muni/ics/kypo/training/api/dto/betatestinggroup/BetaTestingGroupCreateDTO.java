package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Set;

public class BetaTestingGroupCreateDTO {

    @NotNull(message = "{betaTestingGroup.organizers.NotNull.message}")
    private Set<String> organizersLogin;

    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true)
    public Set<String> getOrganizersLogin() {
        return organizersLogin;
    }

    public void setOrganizersLogin(Set<String> organizersLogin) {
        this.organizersLogin = organizersLogin;
    }

    @Override
    public String toString() {
        return "BetaTestingGroupUpdateDTO{" +
                ", organizersLogin=" + organizersLogin +
                '}';
    }
}
