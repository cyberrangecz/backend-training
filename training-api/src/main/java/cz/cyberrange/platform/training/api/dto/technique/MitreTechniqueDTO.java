package cz.cyberrange.platform.training.api.dto.technique;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * One MITRE ATT&amp;CK technique attached to a training level, identified by the key it is
 * catalogued under. A property left null is omitted from the serialized form rather than written as
 * a null.
 */
@Data
@ApiModel(
    value = "MitreTechniqueDTO",
    description =
        "Represent 'how' an trainee achieves a tactical goal of the training level by performing an action.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MitreTechniqueDTO {

  /**
   * Identifies the stored technique. Left unset when the technique travels inside an exported
   * training level, which carries the key alone.
   */
  @ApiModelProperty(value = "Main identifier of Mitre technique.", required = true, example = "1")
  private Long id;

  @ApiModelProperty(example = "T1548.001")
  @NotEmpty(message = "{mitreTechnique.techniqueKey.NotEmpty.message}")
  private String techniqueKey;
}
