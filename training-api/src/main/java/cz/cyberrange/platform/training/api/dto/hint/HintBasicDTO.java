package cz.cyberrange.platform.training.api.dto.hint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/** Contains generally safe, descriptive-only data accessible by both organizers and trainees. */
@Data
@ApiModel(
    value = "HintBasicDTO",
    description = "A brief textual description to aid the participant.")
public class HintBasicDTO {

  @ApiModelProperty(value = "Main identifier of hint.", example = "1")
  protected Long id;

  @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
  @NotEmpty(message = "{hint.title.NotEmpty.message}")
  protected String title;

  @NotNull(message = "{hint.hintPenalty.NotNull.message}")
  @Min(value = 0, message = "{hint.hintPenalty.Min.message}")
  @Max(value = 100, message = "{hint.hintPenalty.Max.message}")
  @ApiModelProperty(
      value = "The number of points the participant loses after receiving the hint.",
      example = "10")
  protected Integer hintPenalty;
}
