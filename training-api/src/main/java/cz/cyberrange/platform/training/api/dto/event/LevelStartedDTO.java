package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Level started event, carrying the {@code type} value {@code level_started} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Records that a trainee entered a level of the training run.")
public class LevelStartedDTO extends TrainingEventDTO {

  /** Name of the started level's {@code EventLevelType} constant (for example {@code TRAINING}) */
  @JsonProperty("level_type")
  private String levelType;

  @JsonProperty("level_title")
  private String levelTitle;

  @JsonProperty("max_score")
  private Integer maxScore;
}
