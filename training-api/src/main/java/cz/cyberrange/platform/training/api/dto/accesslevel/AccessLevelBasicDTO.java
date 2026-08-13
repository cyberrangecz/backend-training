package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "AccessLevelBasicDTO",
    description = "A level containing instructions on how to connect to the virtual machines.",
    parent = AbstractLevelBasicDTO.class)
public class AccessLevelBasicDTO extends AbstractLevelBasicDTO {}
