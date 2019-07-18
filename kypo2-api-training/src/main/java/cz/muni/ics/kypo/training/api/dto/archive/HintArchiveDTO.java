package cz.muni.ics.kypo.training.api.dto.archive;

import io.swagger.annotations.ApiModelProperty;

public class HintArchiveDTO {

    private Long id;
    private String title;
    private String content;
    private Integer hintPenalty;

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
    public String toString() {
        return "HintArchiveDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", hintPenalty=" + hintPenalty +
                '}';
    }
}
