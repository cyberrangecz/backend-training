package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelCreateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An access level of a training definition being created. Carries the {@code ACCESS_LEVEL}
 * discriminator that lets {@link AbstractLevelCreateDTO} resolve this subtype during
 * deserialization.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "The passkey and connection details stored on an access level.")
public class AccessLevelCreateDTO extends AccessLevelContentDTO implements AbstractLevelCreateDTO {}
