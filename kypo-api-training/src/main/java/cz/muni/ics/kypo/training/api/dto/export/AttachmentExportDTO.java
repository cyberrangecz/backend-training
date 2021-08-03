package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
@ApiModel(value = "AttachmentExportDTO", description = "An exported attachment of training level.")
public class AttachmentExportDTO {
    @ApiModelProperty(value = "URL link to file or website.")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttachmentExportDTO)) return false;
        AttachmentExportDTO that = (AttachmentExportDTO) o;
        return Objects.equals(getContent(), that.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContent());
    }

    @Override
    public String toString() {
        return "AttachmentExportDTO{" +
                "content='" + content + '\'' +
                '}';
    }
}
