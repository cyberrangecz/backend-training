package cz.muni.ics.kypo.training.api.dto.infolevel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Encapsulates information needed to update info level.
 *
 */
@ApiModel(value = "InfoLevelUpdateDTO", description = "Info level to update.")
public class InfoLevelUpdateDTO {

    @ApiModelProperty(value = "Main identifier of level.", required = true, example = "4")
    @NotNull(message = "{infolevelupdate.id.NotNull.message}")
    protected Long id;
    @ApiModelProperty(value = "Short textual description of the level.", required = true, example = "Info Level1")
    @NotEmpty(message = "{infolevelupdate.title.NotEmpty.message}")
    protected String title;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Informational stuff")
    @NotEmpty(message = "{infolevelupdate.content.NotEmpty.message}")
    private String content;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
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
