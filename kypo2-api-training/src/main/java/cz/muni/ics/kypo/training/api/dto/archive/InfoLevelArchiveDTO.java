package cz.muni.ics.kypo.training.api.dto.archive;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "InfoLevelArchiveDTO", description = "A HTML content for the participant to read.", parent = AbstractLevelArchiveDTO.class)
public class InfoLevelArchiveDTO extends AbstractLevelArchiveDTO{

    private String content;

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Informational stuff")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "InfoLevelArchiveDTO{" +
                "content='" + content + '\'' +
                '}';
    }
}
