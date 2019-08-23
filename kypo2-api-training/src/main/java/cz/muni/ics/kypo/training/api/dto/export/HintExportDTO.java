package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 *  Encapsulates information about Hint.
 *
 * @author Pavel Seda
 */
public class HintExportDTO {

    private String title;
    private String content;
    private Integer hintPenalty;
    private int order;

    /**
     * Instantiates a new Hint export dto.
     */
    public HintExportDTO() {
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
        if (!(o instanceof HintExportDTO)) return false;
        HintExportDTO that = (HintExportDTO) o;
        return Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getContent(), that.getContent()) &&
                Objects.equals(getHintPenalty(), that.getHintPenalty());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getContent(), getHintPenalty());
    }

    @Override
    public String toString() {
        return "HintExportDTO{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", hintPenalty=" + hintPenalty +
                ", order=" + order +
                '}';
    }
}
