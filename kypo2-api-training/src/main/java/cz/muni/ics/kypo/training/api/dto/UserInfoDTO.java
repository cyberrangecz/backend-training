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

    @Override
    public String toString() {
        return "UserInfoDTO{" +
                "fullName=" + fullName + '\'' +
                ", login='" + login + '\'' +
                '}';
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
}
