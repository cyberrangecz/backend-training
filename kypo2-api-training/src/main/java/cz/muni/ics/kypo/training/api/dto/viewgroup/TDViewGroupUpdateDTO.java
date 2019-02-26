package cz.muni.ics.kypo.training.api.dto.viewgroup;

import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class TDViewGroupUpdateDTO {

    @NotNull(message = "{viewGroup.id.NotNull.message}")
    private Long id;
    @NotEmpty(message = "{viewGroup.title.NotEmpty.message}")
    private String title;
    @NotEmpty(message = "{viewGroup.description.NotEmpty.message}")
    private String description;
    @NotNull(message = "{viewGroup.organizers.NotNull.message}")
    private Set<UserInfoDTO> organizers;

    @ApiModelProperty(value = "Main identifier of view group.", required = true, example = "2")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    @ApiModelProperty(value = "Logins of users who is allowed to see training definition.", required = true)
    public Set<UserInfoDTO> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(Set<UserInfoDTO> organizers) {
        this.organizers = organizers;
    }

    @Override
    public String toString() {
        return "TDViewGroupUpdateDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", organizers=" + organizers +
                '}';
    }
}
