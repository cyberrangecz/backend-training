package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "InfoLevelExportDTO", description = "A HTML content for the participant to read.", parent = AbstractLevelExportDTO.class)
public class InfoLevelExportDTO extends AbstractLevelExportDTO {

    private String content;

    public InfoLevelExportDTO() {
        this.content = "";
    }

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Informational stuff")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "InfoLevelExportDTO{" +
                "content='" + content + '\'' +
                '}';
    }
}
