package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda
 */
public class UserInfoDTO {

    //private Long id;
    private String login;
    @JsonProperty(value = "full_name")
    private String fullName;

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }

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
              //  "id=" + id +
                ", login='" + login + '\'' +
                '}';
    }
}
