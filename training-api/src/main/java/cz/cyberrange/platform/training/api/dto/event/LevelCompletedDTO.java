package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Level completed event, carrying the {@code type} value {@code level_completed}. */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "LevelCompletedDTO", description = "Level completed event")
public class LevelCompletedDTO extends TrainingEventDTO {

  /**
   * Name of the completed level's {@code EventLevelType} constant, for example {@code TRAINING}.
   */
  @ApiModelProperty(value = "Level type")
  @JsonProperty("level_type")
  private String levelType;
}
