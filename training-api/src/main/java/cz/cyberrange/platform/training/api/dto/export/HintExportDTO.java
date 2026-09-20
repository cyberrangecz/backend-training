package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Encapsulates information about Hint */
@Data
@NoArgsConstructor
@Schema(description = "An exported hint a trainee can take during a training level")
public class HintExportDTO {

  @Schema(example = "Hint1")
  private String title;

  @Schema(example = "Very good advice")
  private String content;

  @Schema(
      description = "Points added to the run's penalty for the level each time the hint is taken",
      example = "10")
  private Integer hintPenalty;

  /** Position among the hints of the level that carries it, not among the training's levels */
  @Schema(description = "Position among the hints of the level that carries it", example = "1")
  private int order;
}
