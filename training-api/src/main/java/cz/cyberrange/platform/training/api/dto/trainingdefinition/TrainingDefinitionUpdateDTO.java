package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.cyberrange.platform.training.api.enums.TDState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Encapsulates information about Training Definition, intended for edit of the definition */
@Data
@Schema(description = "The training definition content that replaces what is stored.")
public class TrainingDefinitionUpdateDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
  @NotNull(message = "{trainingDefinition.id.NotNull.message}")
  private Long id;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "TrainingDefinition2")
  @NotEmpty(message = "{trainingDefinition.title.NotEmpty.message}")
  private String title;

  @Schema(example = "Unreleased training definition")
  private String description;

  @Schema(example = "[phishing]")
  private String[] prerequisites;

  private String[] outcomes;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "UNRELEASED")
  @NotNull(message = "{trainingDefinition.state.NotNull.message}")
  private TDState state;

  /**
   * When present, its organizer ids replace the beta testing group's membership on the updated
   * definition. Omitting it while the stored definition already has a group is refused as a
   * conflict rather than removing the group.
   */
  @Valid private BetaTestingGroupUpdateDTO betaTestingGroup;

  /**
   * Required by validation but not read by the mapper or the facade; the training definition entity
   * carries no such field
   */
  @Schema(
      description = "Required in the request but not stored.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "false")
  @NotNull(message = "{trainingDefinition.showStepperBar.NotNull.message}")
  private boolean showStepperBar;
}
