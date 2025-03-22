package cz.cyberrange.platform.training.api.dto.jeopardylevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.jeopardylevel.category.JeopardyCategoryDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Encapsulates information about jeopardy level. Inherits from {@link AbstractLevelDTO}
 */
@Getter
@Setter
@ToString
@ApiModel(value = "Jeopardy", description = "A level containing more training levels", parent = AbstractLevelDTO.class)
public class GenericJeopardyLevelDTO<SublevelType> extends AbstractLevelDTO {

    @ApiModelProperty(value = "List of categories of sublevels")
    private List<JeopardyCategoryDTO<SublevelType>> categories;
}
