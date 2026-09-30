package cz.cyberrange.platform.training.api.dto.infolevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents an info level in a listing or summary context. Adds no field of its own beyond {@link
 * AbstractLevelBasicDTO}, so it never carries the level's content.
 */
@Schema(description = "An info level in outline, without the text it presents.")
public class InfoLevelBasicDTO extends AbstractLevelBasicDTO {}
