package cz.muni.ics.kypo.training.api.dto.imports;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import java.util.Objects;

@ApiModel(value = "AttachmentImportDTO", description = "Imported attachment.")
public class AttachmentImportDTO {
    @ApiModelProperty(value = "URL link to file or website.")
    @NotEmpty(message = "{attachment.content.NotEmpty.message}")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "AttachmentImportDTO{" +
                "content='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttachmentImportDTO)) return false;
        AttachmentImportDTO that = (AttachmentImportDTO) o;
        return Objects.equals(getContent(), that.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContent());
    }
}
