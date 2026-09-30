package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Hint taken event, carrying the {@code type} value {@code hint_taken} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Records that a trainee revealed a hint of a training level.")
public class HintTakenDTO extends TrainingEventDTO {

  @JsonProperty("hint_id")
  private Long hintId;

  @JsonProperty("hint_title")
  private String hintTitle;

  @JsonProperty("hint_penalty_points")
  private Integer hintPenaltyPoints;
}
