package cz.cyberrange.platform.training.api.dto;

import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Identifies one level by id, title, order, and level type. Every producer of this type, whether
 * built by hand per level subtype or through {@code LevelMapper}, sets all four fields together.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "One level of a training definition, named and placed in its sequence.")
public class BasicLevelInfoDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  private Long id;

  @Schema(example = "Training Level1")
  private String title;

  @Schema(example = "TRAINING_LEVEL")
  private LevelType levelType;

  @Schema(description = "Zero-based position of the level within its definition.", example = "1")
  private int order;
}
