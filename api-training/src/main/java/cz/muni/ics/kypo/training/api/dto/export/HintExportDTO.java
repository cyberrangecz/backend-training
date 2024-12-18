package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 *  Encapsulates information about Hint.
 *
 */
@ApiModel(value = "HintExportDTO", description = "An exported brief textual description to aid the participant.")
public class HintExportDTO {

    @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
    private String title;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Very good advice")
    private String content;
    @ApiModelProperty(value = "The number of points the participant loses after receiving the hint.", example = "10")
    private Integer hintPenalty;
    @ApiModelProperty(value = "The order of hint in training level", example = "1")
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
