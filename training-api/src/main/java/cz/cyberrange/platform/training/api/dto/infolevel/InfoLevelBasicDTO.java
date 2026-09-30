package cz.cyberrange.platform.training.api.dto.infolevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.annotations.ApiModel;

/**
 * Represents an info level in a listing or summary context. Adds no field of its own beyond {@link
 * AbstractLevelBasicDTO}, so it never carries the level's content.
 */
@ApiModel(
    value = "InfoLevelBasicDTO",
    description = "A HTML content for the participant to read.",
    parent = AbstractLevelBasicDTO.class)
public class InfoLevelBasicDTO extends AbstractLevelBasicDTO {}
