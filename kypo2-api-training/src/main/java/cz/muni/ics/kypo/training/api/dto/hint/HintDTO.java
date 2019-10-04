package cz.muni.ics.kypo.training.api.dto.hint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about Hint.
 */
@ApiModel(value = "HintDTO", description = "A brief textual description to aid the participant.")
public class HintDTO {

    private Long id;
    private String title;
    private String content;
    private Integer hintPenalty;
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

    /**
     * Gets hint penalty.
     *
     * @return the hint penalty
     */
    @ApiModelProperty(value = "The number of points the participant loses after receiving the hint.", example = "10")
    public Integer getHintPenalty() {
        return hintPenalty;
    }

    /**
     * Sets hint penalty.
     *
     * @param hintPenalty the hint penalty
     */
    public void setHintPenalty(Integer hintPenalty) {
        this.hintPenalty = hintPenalty;
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

        HintDTO hintDTO = (HintDTO) o;

        if (!id.equals(hintDTO.id)) return false;
        if (!title.equals(hintDTO.title)) return false;
        if (!content.equals(hintDTO.content)) return false;
        if (order != hintDTO.order) return false;
        return hintPenalty.equals(hintDTO.hintPenalty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, hintPenalty, order);
    }

    @Override
    public String toString() {
        return "HintDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", hintPenalty=" + hintPenalty +
                ", order=" + order +
                '}';
    }
}
