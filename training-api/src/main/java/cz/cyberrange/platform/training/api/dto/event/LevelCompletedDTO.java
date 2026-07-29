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
@ApiModel(value = "LevelCompletedDTO", description = "Level completed event")
public class LevelCompletedDTO extends TrainingEventDTO {

  @ApiModelProperty(value = "Level type")
  @JsonProperty("level_type")
  private String levelType;
}
