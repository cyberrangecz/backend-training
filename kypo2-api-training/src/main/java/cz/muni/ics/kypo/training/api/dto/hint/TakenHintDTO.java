package cz.muni.ics.kypo.training.api.dto.hint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(value = "TakenHintDTO", description = "A taken brief textual description to aid the participant.")
public class TakenHintDTO {

    private Long id;
    private String title;
    private String content;

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
        return "HintDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
