package cz.muni.ics.kypo.training.api.dto.export;

import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * @author Pavel Seda
 */
public class HintExportDTO {

    private String title;
    private String content;
    private Integer hintPenalty;

    public HintExportDTO() {
    }

    @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Very good advice")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @ApiModelProperty(value = "The number of points the participant loses after receiving the hint.", example = "10")
    public Integer getHintPenalty() {
        return hintPenalty;
    }

    public void setHintPenalty(Integer hintPenalty) {
        this.hintPenalty = hintPenalty;
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
        return "HintDTO{" +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", hintPenalty=" + hintPenalty +
                '}';
    }

}
