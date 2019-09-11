package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about User.
 *
 * @author Pavel Seda
 */
public class UserInfoDTO {

    private Long userRefId;
    private String login;
    @JsonProperty(value = "full_name")
    private String fullName;
    @JsonProperty(value = "given_name")
    private String givenName;
    @JsonProperty(value = "family_name")
    private String familyName;
    private String iss;
    private byte[] picture;

    /**
     * Gets login.
     *
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets login.
     *
     * @param login the login
     */
    public void setLogin(String login) {
        this.login = login;
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
    @JsonProperty(value = "user_ref_id")
    public Long getUserRefId() {
        return userRefId;
    }

    /**
     * Sets user ref id.
     *
     * @param userRefId the user ref id
     */
    @JsonProperty(value = "id")
    public void setUserRefId(Long userRefId) {
        this.userRefId = userRefId;
    }

    /**
     * Gets full name.
     *
     * @return the full name
     */
    @ApiModelProperty(value = "Reference to user in another microservice and get his full name", example = "Mgr. Ing. Pavel Šeda")
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets full name.
     *
     * @param fullName the full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets given name.
     *
     * @return the given name
     */
    @ApiModelProperty(value = "User given name", example = "Pavel")
    public String getGivenName() {
        return givenName;
    }

    /**
     * Sets given name.
     *
     * @param givenName the given name
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * Gets family name.
     *
     * @return the family name
     */
    @ApiModelProperty(value = "User family name", example = "Seda")
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Sets family name.
     *
     * @param familyName the family name
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
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
