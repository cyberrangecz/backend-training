package cz.cyberrange.platform.training.api.dto.technique;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * One MITRE ATT&amp;CK technique attached to a training level, identified by the key it is
 * catalogued under. A property left null is omitted from the serialized form rather than written as
 * a null.
 */
@Data
@Schema(description = "One MITRE ATT&CK technique attached to a training level.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MitreTechniqueDTO {

  /**
   * Identifies the stored technique. Left unset when the technique travels inside an exported
   * training level, which carries the key alone.
   */
  @Schema(
      description = "Identifies the stored technique; absent inside an exported level.",
      example = "1")
  private Long id;

  @Schema(
      description =
          "The technique's key in the MITRE ATT&CK catalogue, as TA####.T#### or TA####.T####.###.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "TA0004.T1548.001")
  @NotEmpty(message = "{mitreTechnique.techniqueKey.NotEmpty.message}")
  @Pattern(
      regexp = "^TA\\d{4}\\.T\\d{4}(\\.\\d{3})?$",
      message = "{mitreTechnique.techniqueKey.Pattern.message}")
  private String techniqueKey;
}
