package cz.cyberrange.platform.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Encapsulates information about training definition and its levels. Carries no state and no
 * estimated duration: both are decided when the definition is created
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Schema(description = "A training definition to create, with its levels in the order they take")
@JsonIgnoreProperties({"show_stepper_bar", "variant_sandboxes", "state", "estimated_duration"})
public class ImportTrainingDefinitionDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "TrainingDefinition2")
  @NotEmpty(message = "{trainingDefinition.title.NotEmpty.message}")
  private String title;

  @Schema(example = "Unreleased training definition")
  private String description;

  private String[] prerequisites;

  private String[] outcomes;

  @Valid private List<AbstractLevelImportDTO> levels = new ArrayList<>();

  /**
   * Replaces the level list with a new list holding the same elements, so later mutation of the
   * argument list does not affect this instance.
   *
   * @param levels the list of {@link AbstractLevelImportDTO}
   */
  public void setLevels(List<AbstractLevelImportDTO> levels) {
    this.levels = new ArrayList<>(levels);
  }
}
