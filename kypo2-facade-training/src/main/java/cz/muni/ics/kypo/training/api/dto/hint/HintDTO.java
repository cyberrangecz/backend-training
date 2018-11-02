package cz.muni.ics.kypo.training.api.dto.hint;

import java.util.Objects;

public class HintDTO {
    private Long id;
    private String title;
    private String content;
    private Integer hintPenalty;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getHintPenalty() {
        return hintPenalty;
    }

    public void setHintPenalty(Integer hintPenalty) {
        this.hintPenalty = hintPenalty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HintDTO hintDTO = (HintDTO) o;

        if (!id.equals(hintDTO.id)) return false;
        if (!title.equals(hintDTO.title)) return false;
        if (!content.equals(hintDTO.content)) return false;
        return hintPenalty.equals(hintDTO.hintPenalty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, hintPenalty);
    }

    @Override
    public String toString() {
        return "HintDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", hintPenalty=" + hintPenalty +
                '}';
    }
}
