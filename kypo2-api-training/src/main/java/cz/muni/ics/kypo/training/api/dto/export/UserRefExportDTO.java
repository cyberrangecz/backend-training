package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about user reference.
 */
public class UserRefExportDTO {
	private String userRefLogin;
	private String userRefFullName;

	/**
	 * Gets user reference login.
	 *
	 * @return the user reference login
	 */
	@ApiModelProperty(value = "Reference to user in another microservice.", example = "441048@mail.muni.cz")
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
	@ApiModelProperty(value = "Reference to user in another microservice and get his full name", example = "Mgr. Ing. Pavel Šeda")
	public void setUserRefFullName(String userRefFullName) {
		this.userRefFullName = userRefFullName;
	}

	@Override public String toString() {
		return "UserRefExportDTO{" + "userRefLogin='" + userRefLogin + '\'' + ", userRefFullName='" + userRefFullName + '\'' + '}';
	}
}
