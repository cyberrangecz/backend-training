package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(
    value = "TrainingDefinitionWithLevelsDTO",
    description = "A blueprint of abstract levels.")
public class TrainingDefinitionWithLevelsDTO extends TrainingDefinitionDTO {

  /**
   * Populated by the facade with the full detail of the definition's levels, in presentation order;
   * never derived from the definition entity itself
   */
  @ApiModelProperty(value = "Information about all levels in training definition.")
  private List<AbstractLevelDTO> levels = new ArrayList<>();
}
