package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about user reference.
 *
 * @author Pavel Seda
 */
public class UserRefDTO {

    private Long id;
    private String userRefLogin;
    private String userRefFullName;
    private String userRefGivenName;
    private String userRefFamilyName;

    /**
     * Gets id.
     *
     * @return the id
     */
    @ApiModelProperty(value = "Main identifier of participant ref.", example = "1")
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets user ref login.
     *
     * @return the user ref login
     */
    @ApiModelProperty(value = "Reference to user in another microservice.", example = "441048@mail.muni.cz")
    public String getUserRefLogin() {
        return userRefLogin;
    }

    /**
     * Sets user ref login.
     *
     * @param userRefLogin the user ref login
     */
    public void setUserRefLogin(String userRefLogin) {
        this.userRefLogin = userRefLogin;
    }

    /**
     * Gets user ref full name.
     *
     * @return the user ref full name
     */
    @ApiModelProperty(value = "Reference to user in another microservice and get his full name", example = "Mgr. Ing. Pavel Šeda")
    public String getUserRefFullName() {
        return userRefFullName;
    }

    /**
     * Sets user ref full name.
     *
     * @param userRefFullName the user ref full name
     */
    public void setUserRefFullName(String userRefFullName) {
        this.userRefFullName = userRefFullName;
    }

    /**
     * Gets user ref given name.
     *
     * @return the user ref given name
     */
    @ApiModelProperty(value = "User given name", example = "Pavel")
    public String getUserRefGivenName() {
        return userRefGivenName;
    }

    /**
     * Sets user ref given name.
     *
     * @param userRefGivenName the user ref given name
     */
    public void setUserRefGivenName(String userRefGivenName) {
        this.userRefGivenName = userRefGivenName;
    }

    /**
     * Gets user ref family name.
     *
     * @return the user ref family name
     */
    @ApiModelProperty(value = "User family name", example = "Seda")
    public String getUserRefFamilyName() {
        return userRefFamilyName;
    }

    /**
     * Sets user ref family name.
     *
     * @param userRefFamilyName the user ref family name
     */
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
