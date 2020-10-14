package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about user reference.
 */
@ApiModel(value = "UserRefExportDTO", description = "An exported information about user reference.")
public class UserRefExportDTO {

    @ApiModelProperty(value = "Reference to user in another microservice.", example = "999999@mail.muni.cz")
    private String userRefLogin;
    @ApiModelProperty(value = "Reference to user in another microservice and get his full name", example = "Mgr. John Doe")
    private String userRefFullName;
    @ApiModelProperty(value = "User given name", example = "John")
    private String userRefGivenName;
    @ApiModelProperty(value = "User family name", example = "Doe")
    private String userRefFamilyName;
    @ApiModelProperty(value = "Reference to user in another microservice and get his iss", example = "https://oidc.muni.cz")
    private String iss;
    @ApiModelProperty(value = "Reference to user in another microservice and get his id", example = "1")
    private Long userRefId;

    /**
     * Gets user reference login.
     *
     * @return the user reference login
     */
    public String getUserRefLogin() {
        return userRefLogin;
    }

    /**
     * Sets user reference login.
     *
     * @param userRefLogin the user reference login
     */
    public void setUserRefLogin(String userRefLogin) {
        this.userRefLogin = userRefLogin;
    }

    /**
     * Gets user reference full name.
     *
     * @return the user reference full name
     */
    public String getUserRefFullName() {
        return userRefFullName;
    }

    /**
     * Sets user reference full name.
     *
     * @param userRefFullName the user reference full name
     */
    public void setUserRefFullName(String userRefFullName) {
        this.userRefFullName = userRefFullName;
    }

    /**
     * Gets iss.
     *
     * @return the iss
     */
    public String getIss() {
        return iss;
    }

    /**
     * Sets iss.
     *
     * @param iss the iss
     */
    public void setIss(String iss) {
        this.iss = iss;
    }

    /**
     * Gets user ref id.
     *
     * @return the user ref id
     */
    public Long getUserRefId() {
        return userRefId;
    }

    /**
     * Sets user ref id.
     *
     * @param userRefId the user ref id
     */
    public void setUserRefId(Long userRefId) {
        this.userRefId = userRefId;
    }

    /**
     * Gets user ref given name.
     *
     * @return the user ref given name
     */
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
        return "UserRefExportDTO{" +
                "userRefLogin='" + userRefLogin + '\'' +
                ", userRefFullName='" + userRefFullName + '\'' +
                ", userRefGivenName='" + userRefGivenName + '\'' +
                ", userRefFamilyName='" + userRefFamilyName + '\'' +
                ", iss='" + iss + '\'' +
                ", userRefId=" + userRefId +
                '}';
    }
}
