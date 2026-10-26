package cz.cyberrange.platform.training.api.dto.infolevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelCreateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An info level of a training definition being created. Carries the {@code INFO_LEVEL}
 * discriminator that lets {@link AbstractLevelCreateDTO} resolve this subtype during
 * deserialization.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "The text an info level presents.")
public class InfoLevelCreateDTO extends InfoLevelContentDTO implements AbstractLevelCreateDTO {}
