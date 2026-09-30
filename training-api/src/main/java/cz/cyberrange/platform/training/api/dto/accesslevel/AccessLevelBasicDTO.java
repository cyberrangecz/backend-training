package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents an access level in a listing or summary context. Adds no field of its own beyond
 * {@link AbstractLevelBasicDTO}, so it never carries the level's passkey or connection content.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An access level in outline, without its passkey or connection details.")
public class AccessLevelBasicDTO extends AbstractLevelBasicDTO {}
