package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import java.util.Objects;

public class TrainingDefinitionInfoDTO {

    private Long id;
    private String title;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingDefinitionInfoDTO that = (TrainingDefinitionInfoDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    @Override
    public String toString() {
        return "TrainingDefinitionInfoDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
