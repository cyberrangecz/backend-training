package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.AbstractLevelCreateDTO;
import cz.cyberrange.platform.training.api.enums.TDState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
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
   * Levels of the new definition in presentation order, none when left out; a level carries no id,
   * and the definition's estimated duration is the sum of their estimated durations
   */
  @Schema(
      description =
          "The definition's levels in order, none when left out. Levels and their hints,"
              + " questions, choices and matching items carry no ids; the server assigns them and"
              + " a level carrying an id is refused.")
  @NotNull(message = "{trainingDefinition.levels.NotNull.message}")
  private List<
          @Valid @NotNull(message = "{trainingDefinition.level.NotNull.message}")
          AbstractLevelCreateDTO>
      levels = new ArrayList<>();
}
