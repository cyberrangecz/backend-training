package cz.muni.ics.kypo.training.api.dto.archive;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about info level. Inherits from {@link AbstractLevelArchiveDTO}
 * Used for archiving.
 */
@ApiModel(value = "InfoLevelArchiveDTO", description = "Archived info level.", parent = AbstractLevelArchiveDTO.class)
public class InfoLevelArchiveDTO extends AbstractLevelArchiveDTO{

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Informational stuff")
    private String content;

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

    @Override
    public String toString() {
        return "InfoLevelArchiveDTO{" +
                "content='" + content + '\'' +
                '}';
    }
}
