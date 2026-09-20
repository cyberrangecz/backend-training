package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Level completed event, carrying the {@code type} value {@code level_completed} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Records that a trainee finished a level of the training run.")
public class LevelCompletedDTO extends TrainingEventDTO {

  /** Name of the completed level's {@code EventLevelType} constant, for example {@code TRAINING} */
  @JsonProperty("level_type")
  private String levelType;
}
