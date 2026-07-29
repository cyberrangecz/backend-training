package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(
    value = "AccessLevelBasicDTO",
    description = "A level containing instructions on how to connect to the virtual machines.",
    parent = AbstractLevelBasicDTO.class)
public class AccessLevelBasicDTO extends AbstractLevelBasicDTO {}
