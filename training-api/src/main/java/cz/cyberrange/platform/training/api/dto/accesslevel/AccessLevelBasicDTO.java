package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.annotations.ApiModel;
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
@ApiModel(
    value = "AccessLevelBasicDTO",
    description = "A level containing instructions on how to connect to the virtual machines.",
    parent = AbstractLevelBasicDTO.class)
public class AccessLevelBasicDTO extends AbstractLevelBasicDTO {}
