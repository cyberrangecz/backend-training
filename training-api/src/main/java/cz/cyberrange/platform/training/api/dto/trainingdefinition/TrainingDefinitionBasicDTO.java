package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Encapsulates information about Training Definition together with the outline of its levels held
 * as {@link AbstractLevelBasicDTO}, which is safe for both organizers and trainees
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "TrainingDefinitionBasicDTO", description = "A blueprint of abstract levels.")
public class TrainingDefinitionBasicDTO extends AbstractTrainingDefinitionDTO {

  /**
   * Populated by the facade from the definition's levels, in presentation order; never derived from
   * the definition entity itself
   */
  @ApiModelProperty(value = "All levels in the training definition, ordered by their order.")
  protected List<AbstractLevelBasicDTO> levels = new ArrayList<>();
}
