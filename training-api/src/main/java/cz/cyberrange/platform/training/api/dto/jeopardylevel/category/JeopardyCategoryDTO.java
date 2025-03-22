package cz.cyberrange.platform.training.api.dto.jeopardylevel.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@ApiModel(value = "JeopardyCategoryDTO", description = "A category inside Jeopardy level.")
public class JeopardyCategoryDTO<SublevelType> {

    @ApiModelProperty(value = "Title of the category", example = "Database exploits")
    private String title;

    @ApiModelProperty(value = "Color of the category", allowableValues = "Any 4 byte integer")
    private int color;

    @ApiModelProperty(value = "List of contained sublevels")
    private List<SublevelType> sublevels;
}
