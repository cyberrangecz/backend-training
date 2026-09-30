package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Encapsulates information about Training Definition together with the complete detail of its
 * levels held as {@link AbstractLevelDTO}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "A training definition with the full detail of its levels.")
public class TrainingDefinitionWithLevelsDTO extends TrainingDefinitionDTO {

  /**
   * Populated by the facade with the full detail of the definition's levels, in presentation order;
   * never derived from the definition entity itself
   */
  private List<AbstractLevelDTO> levels = new ArrayList<>();
}
