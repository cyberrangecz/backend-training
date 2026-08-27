package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Encapsulates the MITRE techniques used by a Training Definition */
@Data
@AllArgsConstructor
@ApiModel(
    value = "TrainingDefinitionMitreTechniquesDTO",
    description = "Training definition with all MITRE techniques used by its training levels.")
public class TrainingDefinitionMitreTechniquesDTO {

  @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
  private Long id;

  @ApiModelProperty(
      value = "A name of the training/game (e.g., Photo Hunter).",
      example = "TrainingDefinition2")
  private String title;

  /**
   * True when the requesting user's own user reference id appears as a participant on a training
   * run created from this definition
   */
  @ApiModelProperty(
      value = "Indicates whether the requesting user has played the training definition.",
      example = "true")
  private boolean played;

  /**
   * Built by the facade one technique at a time from the query results for the definition, never
   * mapped as a whole
   */
  @ApiModelProperty(
      value = "Distinct MITRE technique keys used by the training levels of the definition.",
      example = "[\"TA0042.T1588.006\", \"TA0043.T1595\"]")
  private List<String> mitreTechniques;
}
