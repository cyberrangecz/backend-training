package cz.cyberrange.platform.training.api.dto.infolevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information needed to update info level. */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "InfoLevelUpdateDTO", description = "Info level to update.")
public class InfoLevelUpdateDTO extends AbstractLevelUpdateDTO {

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Informational stuff")
  @NotEmpty(message = "{infoLevel.content.NotEmpty.message}")
  private String content;

  public InfoLevelUpdateDTO() {
    this.levelType = LevelType.INFO_LEVEL;
  }
}
