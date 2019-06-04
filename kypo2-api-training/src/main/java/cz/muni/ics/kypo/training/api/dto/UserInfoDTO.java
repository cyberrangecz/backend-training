package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * @author Pavel Seda
 */
public class UserInfoDTO {

    private String login;
    @JsonProperty(value = "full_name")
    private String fullName;
    @JsonProperty(value="given_name")
    private String givenName;
    @JsonProperty(value="family_name")
    private String familyName;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @ApiModelProperty(value = "Reference to user in another microservice and get his full name", example = "Mgr. Ing. Pavel Šeda")
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @ApiModelProperty(value = "User given name", example = "Pavel")
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    @ApiModelProperty(value = "User family name", example = "Seda")
    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfoDTO that = (UserInfoDTO) o;
        return Objects.equals(login, that.login) &&
                Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, fullName);
    }

    @Override
    public String toString() {
        return "UserInfoDTO{" +
                "login='" + login + '\'' +
                ", fullName='" + fullName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                '}';
    }
}
