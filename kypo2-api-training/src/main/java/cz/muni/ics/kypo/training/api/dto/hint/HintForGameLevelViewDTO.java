package cz.muni.ics.kypo.training.api.dto.hint;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class HintForGameLevelViewDTO {

    private Long id;
    private String title;

    @ApiModelProperty(value = "Main identifier of hint.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
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
        HintForGameLevelViewDTO that = (HintForGameLevelViewDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title);
    }

    @Override
    public String toString() {
        return "HintForGameLevelViewDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}
