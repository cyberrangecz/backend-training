package cz.muni.ics.kypo.training.api.dto.betatestinggroup;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Encapsulates information needed for creation of new Beta testing group.
 */
public class BetaTestingGroupCreateDTO {

    @NotNull(message = "{betaTestingGroup.organizersLogin.NotNull.message}")
    private Set<String> organizersLogin;

    /**
     * Gets organizers login.
     *
     * @return the organizers login
     */
    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true)
    public Set<String> getOrganizersLogin() {
        return organizersLogin;
    }

    /**
     * Sets organizers login.
     *
     * @param organizersLogin the organizers login
     */
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
