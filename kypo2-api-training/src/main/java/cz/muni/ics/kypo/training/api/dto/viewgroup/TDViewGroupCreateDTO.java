package cz.muni.ics.kypo.training.api.dto.viewgroup;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class TDViewGroupCreateDTO {

    @NotEmpty(message = "{viewGroup.title.NotEmpty.message}")
    private String title;
    @NotEmpty(message = "{viewGroup.description.NotEmpty.message}")
    private String description;
    @NotNull(message = "{viewGroup.orgIds.NotNull.message}")
    private Set<String> organizerLogins;

    @ApiModelProperty(value = "A title of the view group.", required = true, example = "Dave organizers group")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "A description of the view group.", required = true, example = "This people are allowed to see my training definition.")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true, example = "[carl]")
    public Set<String> getOrganizerLogins() {
        return organizerLogins;
    }

    public void setOrganizerLogins(Set<String> organizerLogins) {
        this.organizerLogins = organizerLogins;
    }

    @Override
    public String toString() {
        return "TDViewGroupUpdateDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", organizerLogins=" + organizerLogins +
                '}';
    }
}
