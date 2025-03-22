package cz.cyberrange.platform.training.api.dto.jeopardylevel.category;

import cz.cyberrange.platform.training.api.dto.jeopardylevel.sublevel.JeopardySublevelUpdateDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@ApiModel(value = "JeopardyCategoryUpdateDTO", description = "A category inside Jeopardy level to update.")
public class JeopardyCategoryUpdateDTO {

    @Size(max = 25, message = "{jeopardycategory.title.Size.message}")
    @NotNull(message = "{jeopardycategory.title.NotEmpty.message}")
    @ApiModelProperty(value = "Title of the category", example = "Database exploits")
    private String title;

    @NotNull(message = "{jeopardycategory.color.NotEmpty.message}")
    @ApiModelProperty(value = "Color of the category", allowableValues = "Any 4 byte integer")
    private int color;

    @Valid
    @ApiModelProperty(value = "List of contained sublevels")
    private List<JeopardySublevelUpdateDTO> sublevels = new ArrayList<>();
}
