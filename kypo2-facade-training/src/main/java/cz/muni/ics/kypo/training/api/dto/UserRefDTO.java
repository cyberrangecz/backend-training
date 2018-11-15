package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

public class UserRefDTO {
    private Long id;
    private Long userRefId;

    @ApiModelProperty(value = "Main identifier of user ref.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Reference to user in another microservice.", example = "1")
    public Long getUserRefId() {
        return userRefId;
    }

    public void setUserRefId(Long userRefId) {
        this.userRefId = userRefId;
    }

    @Override
    public String toString() {
        return "UserRefDTO{" + "id=" + id + ", userRefId=" + userRefId + '}';
    }
}
