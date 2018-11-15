package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

public class AuthorRefDTO {
    private Long id;
    private String authorRefLogin;

    @ApiModelProperty(value = "Main identifier of author.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Reference to user in another microservice.", example = "Designer1")
    public String getAuthorRefLogin() {
        return authorRefLogin;
    }

    public void setAuthorRefLogin(String authorRefLogin) {
        this.authorRefLogin = authorRefLogin;
    }

    @Override
    public String toString() {
        return "AuthorRefDTO{" +
                "id=" + id +
                ", authorRefLogin='" + authorRefLogin + '\'' +
                '}';
    }
}
