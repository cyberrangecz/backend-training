package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class UserDTO {

    private Long id;
    private String login;
    @JsonProperty(value = "full_name")
    private String fullName;
    @JsonProperty(value = "given_name")
    private String givenName;
    @JsonProperty(value = "family_name")
    private String familyName;

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserDTO)) return false;
        UserDTO userDTO = (UserDTO) object;
        return Objects.equals(getId(), userDTO.getId()) &&
                Objects.equals(getLogin(), userDTO.getLogin()) &&
                Objects.equals(getFullName(), userDTO.getFullName()) &&
                Objects.equals(getGivenName(), userDTO.getGivenName()) &&
                Objects.equals(getFamilyName(), userDTO.getFamilyName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLogin(), getFullName(), getGivenName(), getFamilyName());
    }


}
