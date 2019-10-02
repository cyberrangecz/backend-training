package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about user reference.
 *
 * @author Pavel Seda
 */
public class UserRefDTO {

    private Long userRefId;
    @JsonProperty("login")
    private String userRefLogin;
    @JsonProperty("full_name")
    private String userRefFullName;
    @JsonProperty("given_name")
    private String userRefGivenName;
    @JsonProperty("family_name")
    private String userRefFamilyName;
    private String iss;
    private byte[] picture;

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
     * Gets iss.
     *
     * @return the iss
     */
    @ApiModelProperty(value = "Reference to user in another microservice and get his iss", example = "https://oidc.muni.cz")
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
    @ApiModelProperty(value = "Reference to user in another microservice and get his id", example = "1")
    @JsonProperty("user_ref_id")
    public Long getUserRefId() {
        return userRefId;
    }

    /**
     * Sets user ref id.
     *
     * @param userRefId the user ref id
     */
    @JsonAlias({"id", "user_ref_id"})
    public void setUserRefId(Long userRefId) {
        this.userRefId = userRefId;
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

    /**
     * Gets the identicon of the user encoded in base64.
     *
     * @return identicon of the user.
     */
    @ApiModelProperty(value = "Identicon of a user.", example = "iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAYAAAA4TnrqAAACIUlEQVR4Xu3YsY0dSQxAQQUlpXT5Z3CS/YgxSrQa4gLlEOBb9pj/x6//fv7/t/78/XhN3yBWyz3kBX2DWC33kBf0DWK13ENe0DeI1XIPeUHfIFbLPeQFfYNYLfeQF/QNYrXcQ17QN4jVcg95Qd8gVss95AV9g1gt95AX9A1itdxDXtA3iNVyD3lB3yBWyz3kBX2DWC33kBf0DWLERGOiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS6z+8b/mPha4jwXuY4H7WOA+FriPBe5jgftY4D4WuI8F7mOB+1jgPha4jwXGbzbn2xicb2Nwvo3B+TYG59sYnG9jcL6Nwfk2BufbGJxvY3C+jcH5Ngbn2xicb2Nwvq1+z2pMtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3RlvgNt34wfeJElG8AAAAASUVORK5CYII=")
    public byte[] getPicture() {
        return picture;
    }

    /**
     * Sets the identicon of the user encoded in base64.
     *
     * @param picture encoded identicon of the user.
     */
    public void setPicture(byte[] picture) {
        this.picture = picture;
    }


    @Override
    public String toString() {
        return "UserRefDTO{" +
                ", userRefLogin='" + userRefLogin + '\'' +
                ", userRefFullName='" + userRefFullName + '\'' +
                ", userRefGivenName='" + userRefGivenName + '\'' +
                ", userRefFamilyName='" + userRefFamilyName + '\'' +
                ", iss='" + iss + '\'' +
                ", userRefId=" + userRefId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRefDTO)) return false;
        UserRefDTO that = (UserRefDTO) o;
        return Objects.equals(getUserRefId(), that.getUserRefId()) &&
                Objects.equals(getUserRefLogin(), that.getUserRefLogin()) &&
                Objects.equals(getUserRefFullName(), that.getUserRefFullName()) &&
                Objects.equals(getUserRefGivenName(), that.getUserRefGivenName()) &&
                Objects.equals(getUserRefFamilyName(), that.getUserRefFamilyName()) &&
                Objects.equals(getIss(), that.getIss());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserRefId(), getUserRefLogin(), getUserRefFullName(), getUserRefGivenName(), getUserRefFamilyName(), getIss());
    }
}
