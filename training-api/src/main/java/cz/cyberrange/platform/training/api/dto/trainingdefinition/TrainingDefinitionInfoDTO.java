package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.enums.TDState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Encapsulates basic information about Training Definition */
@Data
@Schema(description = "A training definition reduced to its identity and state.")
public class TrainingDefinitionInfoDTO implements InstanceCountHolder {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "TrainingDefinition2")
  private String title;

  @Schema(example = "UNRELEASED")
  private TDState state;

  /** Resolved from the persisted training instances when the DTO is built; never stored */
  @Schema(
      description = "Number of training instances created from the definition, past ones included.",
      example = "3")
  private long instanceCount;
}
