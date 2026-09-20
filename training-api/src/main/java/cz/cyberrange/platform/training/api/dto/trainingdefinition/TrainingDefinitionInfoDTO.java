package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.enums.TDState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Encapsulates basic information about Training Definition */
@Data
@Schema(description = "A training definition reduced to its identity and state.")
public class TrainingDefinitionInfoDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "TrainingDefinition2")
  private String title;

  @Schema(example = "UNRELEASED")
  private TDState state;
}
