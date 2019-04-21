package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import java.util.Objects;

public class TrainingDefinitionInfoDTO {

    private Long id;
    private String title;
    private Boolean canEdit;

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

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingDefinitionInfoDTO that = (TrainingDefinitionInfoDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(canEdit, that.canEdit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, canEdit);
    }

    @Override
    public String toString() {
        return "TrainingDefinitionInfoDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", canEdit=" + canEdit +
                '}';
    }
}
