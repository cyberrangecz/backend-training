package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString(callSuper = true)
@ApiModel(value = "LevelStartedDTO", description = "Level started event")
public class LevelStartedDTO extends TrainingEventDTO {

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
