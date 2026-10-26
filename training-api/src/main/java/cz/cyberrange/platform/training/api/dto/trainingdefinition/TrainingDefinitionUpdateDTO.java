package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * The whole editable content of a training definition, its levels included, written in one
 * transaction over what is stored. The definition's state is not part of it.
 */
@Data
@Schema(description = "The training definition content and levels that replace what is stored.")
public class TrainingDefinitionUpdateDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "TrainingDefinition2")
  @NotEmpty(message = "{trainingDefinition.title.NotEmpty.message}")
  private String title;

  @Schema(example = "Unreleased training definition")
  private String description;

  @Schema(example = "[phishing]")
  private String[] prerequisites;

  private String[] outcomes;

  /**
   * The version the caller last read; the write is refused when the stored definition carries
   * another one
   */
  @Schema(
      description = "The version of the definition the change was made on, as last read.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "4")
  @NotNull(message = "{trainingDefinition.version.NotNull.message}")
  private Long version;

  /**
   * Every level the definition keeps, in presentation order. A level without an id is created, a
   * level with the id of one of the definition's levels replaces that level, and a stored level
   * left out is deleted.
   */
  @Schema(
      description =
          "All levels in order: one without an id is created, one with the id of a level of"
              + " this definition replaces it, and a stored level left out is deleted. New levels,"
              + " hints, questions, choices and matching items carry no ids; the server assigns"
              + " them.",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "{trainingDefinition.levels.NotNull.message}")
  private List<
          @Valid @NotNull(message = "{trainingDefinition.level.NotNull.message}")
          AbstractLevelUpdateDTO>
      levels;
}
