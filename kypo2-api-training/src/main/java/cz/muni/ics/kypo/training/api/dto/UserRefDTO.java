package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda
 */
public class UserRefDTO {

    private Long id;
    private String userRefLogin;
    private String userRefFullName;

    @ApiModelProperty(value = "Main identifier of participant ref.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Reference to user in another microservice.", example = "441048@mail.muni.cz")
    public String getUserRefLogin() {
        return userRefLogin;
    }

    public void setUserRefLogin(String userRefLogin) {
        this.userRefLogin = userRefLogin;
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
        return "UserRefDTO{" +
                "id=" + id +
                ", userRefLogin='" + userRefLogin + '\'' +
                '}';
    }
}
