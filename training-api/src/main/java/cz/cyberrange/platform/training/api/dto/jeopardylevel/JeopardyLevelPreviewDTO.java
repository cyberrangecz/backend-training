package cz.cyberrange.platform.training.api.dto.jeopardylevel;

import cz.cyberrange.platform.training.api.dto.jeopardylevel.sublevel.JeopardySublevelPreviewDTO;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "JeopardyLevelPreViewDTO", description = "A preview of Jeopardy level", parent = GenericJeopardyLevelDTO.class)
public class JeopardyLevelPreviewDTO extends GenericJeopardyLevelDTO<JeopardySublevelPreviewDTO> {
}
