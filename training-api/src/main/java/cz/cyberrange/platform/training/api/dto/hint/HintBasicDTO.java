package cz.cyberrange.platform.training.api.dto.hint;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * A hint stripped of its advice: what it is called and what taking it costs, without the text a
 * trainee would read
 */
@Data
@Schema(description = "A hint's title and point cost, without the advice itself.")
public class HintBasicDTO {

  @Schema(example = "1")
  protected Long id;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Hint1")
  @NotEmpty(message = "{hint.title.NotEmpty.message}")
  protected String title;

  /** Points deducted from the level's score once the hint is taken; accepted between 0 and 100 */
  @NotNull(message = "{hint.hintPenalty.NotNull.message}")
  @Min(value = 0, message = "{hint.hintPenalty.Min.message}")
  @Max(value = 100, message = "{hint.hintPenalty.Max.message}")
  @Schema(
      description = "Points taken off the level's score once the hint is taken.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "10")
  protected Integer hintPenalty;
}
