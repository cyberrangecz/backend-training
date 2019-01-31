package cz.muni.ics.kypo.training.api.dto.viewgroup;

import javax.validation.constraints.NotNull;
import java.util.Set;

public class TDViewGroupUpdateDTO {

    @NotNull(message = "{viewGroup.id.NotNull.message}")
    private Long id;
    @NotNull(message = "{viewGroup.title.NotNull.message}")
    private String title;
    private String description;
    @NotNull(message = "{viewGroup.orgIds.NotNull.message}")
    private Set<String> organizerLogins;

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

    public Set<String> getOrganizerLogins() {
        return organizerLogins;
    }

    public void setOrganizerLogins(Set<String> organizerLogins) {
        this.organizerLogins = organizerLogins;
    }

    @Override
    public String toString() {
        return "TDViewGroupUpdateDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", organizerLogins=" + organizerLogins +
                '}';
    }
}
