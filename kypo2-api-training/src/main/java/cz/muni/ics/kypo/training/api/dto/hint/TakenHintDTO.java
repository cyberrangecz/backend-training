package cz.muni.ics.kypo.training.api.dto.hint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about hint that was already taken
 */
@ApiModel(value = "TakenHintDTO", description = "A taken brief textual description to aid the participant.")
public class TakenHintDTO {

    private Long id;
    private String title;
    private String content;
    private int order;

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

    /**
     * Gets content.
     *
     * @return the content
     */
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Very good advice")
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    @ApiModelProperty(value = "The order of hint in game level", example = "1")
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TakenHintDTO hintDTO = (TakenHintDTO) o;

        if (!id.equals(hintDTO.id)) return false;
        if (!title.equals(hintDTO.title)) return false;
        return content.equals(hintDTO.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content);
    }

    @Override
    public String toString() {
        return "TakenHintDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", order=" + order +
                '}';
    }
}
