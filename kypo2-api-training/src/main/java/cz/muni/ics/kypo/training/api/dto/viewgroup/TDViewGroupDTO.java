package cz.muni.ics.kypo.training.api.dto.viewgroup;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;

import javax.validation.constraints.NotNull;
import java.util.Set;

public class TDViewGroupDTO {

    private Long id;
    private String title;
    private String description;
    private Set<UserRefDTO> organizers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<UserRefDTO> getOrganizers() {
        return organizers;
    }

    public void setOrganizers(Set<UserRefDTO> organizers) {
        this.organizers = organizers;
    }

    @Override
    public String toString() {
        return "TDViewGroupDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", organizers=" + organizers +
                '}';
    }
}
