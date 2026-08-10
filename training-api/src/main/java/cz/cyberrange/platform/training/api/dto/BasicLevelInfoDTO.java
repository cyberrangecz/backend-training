package cz.cyberrange.platform.training.api.dto;

import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Encapsulates basic information about level. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(
    value = "BasicLevelInfoDTO",
    description = "Basic information about the level and its type.")
public class BasicLevelInfoDTO {

  @ApiModelProperty(value = "Main identifier of level.", example = "1")
  private Long id;

  @ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
  private String title;

  @ApiModelProperty(value = "Type of the level.", example = "TRAINING")
  private LevelType levelType;

  @ApiModelProperty(
      value = "Order of level among levels in training definition starting from 0.",
      example = "1")
  private int order;
}
