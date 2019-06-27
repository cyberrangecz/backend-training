package cz.muni.ics.kypo.training.api.dto.infolevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about game level. Inherits from {@link AbstractLevelDTO}
 *
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "InfoLevelDTO", description = "A HTML content for the participant to read.", parent = AbstractLevelDTO.class)
public class InfoLevelDTO extends AbstractLevelDTO {

    private String content;

    /**
     * Gets content.
     *
     * @return the content
     */
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Informational stuff")
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
        return "InfoLevelDTO [content=" + content + ", toString()=" + super.toString() + "]";
    }

}
