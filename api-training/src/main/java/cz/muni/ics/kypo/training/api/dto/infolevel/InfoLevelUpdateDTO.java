package cz.muni.ics.kypo.training.api.dto.infolevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Encapsulates information needed to update info level.
 *
 */
@ApiModel(value = "InfoLevelUpdateDTO", description = "Info level to update.")
public class InfoLevelUpdateDTO extends AbstractLevelUpdateDTO {

    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Informational stuff")
    @NotEmpty(message = "{infoLevel.content.NotEmpty.message}")
    private String content;

    public InfoLevelUpdateDTO() {
        this.levelType = LevelType.INFO_LEVEL;
    }

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
        StringBuilder builder = new StringBuilder();
        builder.append("InfoLevelUpdateDTO [id=");
        builder.append(id);
        builder.append(", title=");
        builder.append(title);
        builder.append(", content=");
        builder.append(content);
        builder.append("]");
        return builder.toString();
    }

}
