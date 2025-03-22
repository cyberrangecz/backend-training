package cz.cyberrange.platform.training.api.dto.jeopardylevel;

import cz.cyberrange.platform.training.api.dto.jeopardylevel.sublevel.JeopardySublevelDTO;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "JeopardyLevelDTO", description = "A level containing more training levels", parent = GenericJeopardyLevelDTO.class)
public class JeopardyLevelDTO extends GenericJeopardyLevelDTO<JeopardySublevelDTO> {
}
