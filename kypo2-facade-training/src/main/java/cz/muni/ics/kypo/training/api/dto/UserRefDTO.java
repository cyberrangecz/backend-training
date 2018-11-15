package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

public class UserRefDTO {
    private Long id;
    private String userRefLogin;

    @ApiModelProperty(value = "Main identifier of user ref.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Reference to user in another microservice.", example = "Organizer1")
    public String getUserRefLogin() {
        return userRefLogin;
    }

    public void setUserRefLogin(String userRefLogin) {
        this.userRefLogin = userRefLogin;
    }

    @Override public String toString() {
        return "UserRefDTO{" + "id=" + id + ", userRefLogin='" + userRefLogin + '\'' + '}';
    }
}
