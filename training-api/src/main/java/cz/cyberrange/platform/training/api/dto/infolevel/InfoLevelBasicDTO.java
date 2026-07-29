package cz.cyberrange.platform.training.api.dto.infolevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.annotations.ApiModel;

@ApiModel(
    value = "InfoLevelBasicDTO",
    description = "A HTML content for the participant to read.",
    parent = AbstractLevelBasicDTO.class)
public class InfoLevelBasicDTO extends AbstractLevelBasicDTO {}
