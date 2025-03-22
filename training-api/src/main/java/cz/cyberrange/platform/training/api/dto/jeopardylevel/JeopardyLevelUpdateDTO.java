package cz.cyberrange.platform.training.api.dto.jeopardylevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.jeopardylevel.category.JeopardyCategoryUpdateDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates information needed to update assessment level.
 */
@Getter
@Setter
@ToString
@ApiModel(value = "JeopardyLevelUpdateDTO", description = "Assessment level to update.")
public class JeopardyLevelUpdateDTO extends AbstractLevelUpdateDTO {

    @Valid
    @ApiModelProperty(value = "List of categories of sublevels")
    private List<JeopardyCategoryUpdateDTO> categories = new ArrayList<>();

    public JeopardyLevelUpdateDTO() {
        this.levelType = LevelType.JEOPARDY_LEVEL;
    }
}
