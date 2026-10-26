package cz.cyberrange.platform.training.api.dto;

import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Holds the content every level of a training definition carries in a request, whatever its type.
 * Extended by the content of each level type.
 */
@Data
public abstract class AbstractLevelContentDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Training Level1")
  @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
  protected String title;

  protected LevelType levelType;

  @Schema(description = "Estimated time to solve the level, in minutes.", example = "20")
  @Min(value = 0, message = "{abstractLevel.estimatedDuration.Min.message}")
  protected int estimatedDuration;
}
