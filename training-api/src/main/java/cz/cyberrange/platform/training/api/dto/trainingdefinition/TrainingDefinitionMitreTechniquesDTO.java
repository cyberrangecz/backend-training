package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Encapsulates the MITRE techniques used by a Training Definition */
@Data
@AllArgsConstructor
@Schema(description = "The MITRE ATT&CK techniques one released training definition uses.")
public class TrainingDefinitionMitreTechniquesDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "TrainingDefinition2")
  private String title;

  /**
   * True when the requesting user's own user reference id appears as a participant on a training
   * run created from this definition
   */
  @Schema(
      description = "Whether the requesting user has taken part in a run of this definition.",
      example = "true")
  private boolean played;

  /**
   * Built by the facade one technique at a time from the query results for the definition, never
   * mapped as a whole
   */
  @Schema(
      description = "Technique keys used across the definition's levels.",
      example = "[\"TA0042.T1588.006\", \"TA0043.T1595\"]")
  private List<String> mitreTechniques;
}
