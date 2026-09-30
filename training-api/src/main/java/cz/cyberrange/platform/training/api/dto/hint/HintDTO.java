package cz.cyberrange.platform.training.api.dto.hint;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A hint including the advice it gives, as a designer submits it with a training level and as it is
 * handed to a trainee who has taken it
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "A hint including the advice it gives.")
public class HintDTO extends HintBasicDTO {

  @Schema(
      description = "The advice shown once the trainee takes the hint.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "Very good advice")
  @NotEmpty(message = "{hint.content.NotEmpty.message}")
  private String content;

  /** Position of the hint within its level's sequence of hints */
  @Schema(description = "Position of the hint within its level.", example = "1")
  @Min(value = 0, message = "{hint.order.Min.message}")
  private int order;
}
