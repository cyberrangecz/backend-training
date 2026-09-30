package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Solution displayed event, carrying the {@code type} value {@code solution_displayed} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Records that a trainee revealed the solution of a training level.")
public class SolutionDisplayedDTO extends TrainingEventDTO {

  @JsonProperty("penalty_points")
  private Integer penaltyPoints;
}
