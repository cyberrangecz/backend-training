package cz.cyberrange.platform.training.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Wraps the solution text of a training level so it is served as a JSON object */
@Data
@AllArgsConstructor
@Schema(description = "The solution text of a training level.")
public class SolutionDTO {

  @Schema(example = "This is how you do it")
  private String solution;
}
