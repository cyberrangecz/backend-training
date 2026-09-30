package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.betatestinggroup.BetaTestingGroupCreateDTO;
import cz.cyberrange.platform.training.api.enums.TDState;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/** Encapsulates information about Training definition, intended for creation of new definition */
@Data
@Schema(description = "The content of a training definition being created.")
public class TrainingDefinitionCreateDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Photo Hunter")
  @NotEmpty(message = "{trainingDefinition.title.NotEmpty.message}")
  private String title;

  @Schema(example = "Description of Photo Hunter")
  private String description;

  @Schema(example = "[HTML, http protocol]")
  private String[] prerequisites;

  @Schema(example = "[outcomes]")
  private String[] outcomes;

  /** Stored as given; the service does not restrict which state a new definition may start in */
  @Schema(
      description = "The state the definition starts in; any state is accepted.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "UNRELEASED")
  @NotNull(message = "{trainingDefinition.state.NotNull.message}")
  private TDState state;

  /**
   * When present, its organizer ids replace the group's membership on the created definition; when
   * absent, the definition is created without a beta testing group
   */
  @Valid private BetaTestingGroupCreateDTO betaTestingGroup;

  /** Governs whether the newly created definition is populated with a default set of levels */
  @Schema(
      description = "Whether to also create a first info level and a first access level.",
      example = "false")
  private boolean defaultContent;
}
