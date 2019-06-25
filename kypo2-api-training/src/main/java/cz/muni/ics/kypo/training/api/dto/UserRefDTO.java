package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * @author Pavel Seda
 */
public class UserRefDTO {

    private Long id;
    private String userRefLogin;
    private String userRefFullName;
    private String userRefGivenName;
    private String userRefFamilyName;

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

    @ApiModelProperty(value = "User given name", example = "Pavel")
    public String getUserRefGivenName() {
        return userRefGivenName;
    }

    public void setUserRefGivenName(String userRefGivenName) {
        this.userRefGivenName = userRefGivenName;
    }

    @ApiModelProperty(value = "User family name", example = "Seda")
    public String getUserRefFamilyName() {
        return userRefFamilyName;
    }

    public void setUserRefFamilyName(String userRefFamilyName) {
        this.userRefFamilyName = userRefFamilyName;
    }

    @Override
    public String toString() {
        return "UserRefDTO{" +
                "id=" + id +
                ", userRefLogin='" + userRefLogin + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserRefDTO)) return false;
        UserRefDTO that = (UserRefDTO) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getUserRefLogin(), that.getUserRefLogin()) &&
                Objects.equals(getUserRefFullName(), that.getUserRefFullName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserRefLogin(), getUserRefFullName());
    }
}
