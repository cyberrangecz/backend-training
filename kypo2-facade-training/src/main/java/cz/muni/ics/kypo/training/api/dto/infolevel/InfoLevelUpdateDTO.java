package cz.muni.ics.kypo.training.api.dto.infolevel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "InfoLevelUpdateDTO", description = "Info Level to update.")
public class InfoLevelUpdateDTO {
    @ApiModelProperty(required = true)
    @NotNull(message = "{infolevelupdate.id.NotNull.message}")
    protected Long id;
    @ApiModelProperty(required = true)
    @NotEmpty(message = "{infolevelupdate.title.NotEmpty.message}")
    protected String title;
    private String content;

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
