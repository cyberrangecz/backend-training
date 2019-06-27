package cz.muni.ics.kypo.training.api.dto.hint;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates basic information about Hint
 */
public class HintForGameLevelViewDTO {

    private Long id;
    private String title;

    /**
     * Gets id.
     *
     * @return the id
     */
    @ApiModelProperty(value = "Main identifier of hint.", example = "1")
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
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
