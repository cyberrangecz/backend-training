package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModelProperty;

public class UserRefExportDTO {
	private String userRefLogin;
	private String userRefFullName;

	@ApiModelProperty(value = "Reference to user in another microservice.", example = "441048@mail.muni.cz")
	public String getUserRefLogin() {
		return userRefLogin;
	}

	public void setUserRefLogin(String userRefLogin) {
		this.userRefLogin = userRefLogin;
	}

	public String getUserRefFullName() {
		return userRefFullName;
	}

	@ApiModelProperty(value = "Reference to user in another microservice and get his full name", example = "Mgr. Ing. Pavel Šeda")
	public void setUserRefFullName(String userRefFullName) {
		this.userRefFullName = userRefFullName;
	}

	@Override public String toString() {
		return "UserRefExportDTO{" + "userRefLogin='" + userRefLogin + '\'' + ", userRefFullName='" + userRefFullName + '\'' + '}';
	}
}
