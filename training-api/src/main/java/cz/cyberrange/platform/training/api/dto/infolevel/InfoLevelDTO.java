package cz.cyberrange.platform.training.api.dto.infolevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about info level. Inherits from {@link AbstractLevelDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "InfoLevelDTO",
    description = "A HTML content for the participant to read.",
    parent = AbstractLevelDTO.class)
public class InfoLevelDTO extends AbstractLevelDTO {

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Informational stuff")
  private String content;
}
