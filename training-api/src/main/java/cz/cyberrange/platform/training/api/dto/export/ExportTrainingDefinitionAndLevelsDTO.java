package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Encapsulates information about training definition and its levels */
@Data
@NoArgsConstructor
@Schema(description = "A training definition with its levels, as written to the exported file")
public class ExportTrainingDefinitionAndLevelsDTO {

  @Schema(example = "TrainingDefinition2")
  private String title;

  @Schema(example = "Unreleased training definition")
  private String description;

  private String[] prerequisites;

  private String[] outcomes;

  /** Ordered by each level's position in the training definition */
  private List<AbstractLevelExportDTO> levels = new ArrayList<>();
}
