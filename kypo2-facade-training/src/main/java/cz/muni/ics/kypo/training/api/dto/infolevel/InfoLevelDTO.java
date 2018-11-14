package cz.muni.ics.kypo.training.api.dto.infolevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "InfoLevelDTO", description = "Info Level.", parent = AbstractLevelDTO.class)
public class InfoLevelDTO extends AbstractLevelDTO {

    private String content;

    @ApiModelProperty(value = "The information and experiences that are directed towards an player.")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "InfoLevelDTO [content=" + content + ", toString()=" + super.toString() + "]";
    }

}
