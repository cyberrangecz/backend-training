package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda
 */
public class UserInfoDTO {

    private Long id;
    private String login;
    private String userRefFullName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @ApiModelProperty(value = "Reference to user in another microservice and get his full name", example = "Mgr. Ing. Pavel Šeda")
    public String getUserRefFullName() {
        return userRefFullName;
    }

    public void setUserRefFullName(String userRefFullName) {
        this.userRefFullName = userRefFullName;
    }

    @Override
    public String toString() {
        return "UserInfoDTO{" +
                "id=" + id +
                ", login='" + login + '\'' +
                '}';
    }
}
