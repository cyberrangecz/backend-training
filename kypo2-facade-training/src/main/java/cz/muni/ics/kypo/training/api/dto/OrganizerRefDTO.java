package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda
 */
public class OrganizerRefDTO {

    private Long id;
    private String organizerRefLogin;

    @ApiModelProperty(value = "Main identifier of user ref.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Reference to user in another microservice.", example = "Organizer1")
    public String getOrganizerRefLogin() {
        return organizerRefLogin;
    }

    public void setOrganizerRefLogin(String organizerRefLogin) {
        this.organizerRefLogin = organizerRefLogin;
    }

    @Override
    public String toString() {
        return "OrganizerRefDTO{" + "id=" + id + ", organizerRefLogin='" + organizerRefLogin + '\'' + '}';
    }
}
