package cz.cyberrange.platform.training.api.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/** Encapsulates information about Hint */
@Data
@Schema(description = "A hint to create on a training level, for the trainee to take when stuck")
public class HintImportDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Hint1")
  @NotEmpty(message = "{hint.title.NotEmpty.message}")
  private String title;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Very good advice")
  @NotEmpty(message = "{hint.content.NotEmpty.message}")
  private String content;

  /**
   * Counted, together with every other hint on the same level, against that level's maximum score;
   * the import is rejected if the total exceeds it
   */
  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Points added to the run's penalty for the level each time the hint is taken",
      example = "10")
  @NotNull(message = "{hint.hintPenalty.NotNull.message}")
  @Min(value = 0, message = "{hint.hintPenalty.Min.message}")
  @Max(value = 100, message = "{hint.hintPenalty.Max.message}")
  private Integer hintPenalty;

  /** Carried through to the persisted hint as submitted; import does not renumber it */
  @NotNull(message = "{hint.order.NotNull.message}")
  @Min(value = 0, message = "{hint.order.Min.message}")
  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Position among the hints of the level, kept as submitted",
      example = "1")
  private Integer order;
}
