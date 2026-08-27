package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Level started event, carrying the {@code type} value {@code level_started} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "LevelStartedDTO", description = "Level started event")
public class LevelStartedDTO extends TrainingEventDTO {

  /** Name of the started level's {@code EventLevelType} constant (for example {@code TRAINING}) */
  @ApiModelProperty(value = "Level type")
  @JsonProperty("level_type")
  private String levelType;

  @ApiModelProperty(value = "Level title")
  @JsonProperty("level_title")
  private String levelTitle;

  @ApiModelProperty(value = "Maximum score")
  @JsonProperty("max_score")
  private Integer maxScore;
}
