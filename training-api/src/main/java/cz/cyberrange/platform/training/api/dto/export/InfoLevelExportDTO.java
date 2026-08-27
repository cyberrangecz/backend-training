package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about info level. Inherits from {@link AbstractLevelExportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "InfoLevelExportDTO",
    description = "Exported info level.",
    parent = AbstractLevelExportDTO.class)
public class InfoLevelExportDTO extends AbstractLevelExportDTO {

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Informational stuff")
  private String content;

  /** Sets {@link #content} to an empty string, the value kept when a mapped source has none. */
  public InfoLevelExportDTO() {
    this.content = "";
  }
}
