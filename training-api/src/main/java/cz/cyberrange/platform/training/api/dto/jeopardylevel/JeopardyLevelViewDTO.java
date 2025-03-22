package cz.cyberrange.platform.training.api.dto.jeopardylevel;

import cz.cyberrange.platform.training.api.dto.jeopardylevel.sublevel.JeopardySublevelViewDTO;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "JeopardyLevelDTO", description = "A view of Jeopardy level", parent = GenericJeopardyLevelDTO.class)
public class JeopardyLevelViewDTO extends GenericJeopardyLevelDTO<JeopardySublevelViewDTO> {
}
