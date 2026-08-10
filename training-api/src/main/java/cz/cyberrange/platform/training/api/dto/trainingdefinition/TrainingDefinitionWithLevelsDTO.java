package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Encapsulates information about Training Definition together with the complete detail of its
 * levels held as {@link AbstractLevelDTO}.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString(callSuper = true)
@ApiModel(
    value = "TrainingDefinitionWithLevelsDTO",
    description = "A blueprint of abstract levels.")
public class TrainingDefinitionWithLevelsDTO extends TrainingDefinitionDTO {

  @ApiModelProperty(value = "Information about all levels in training definition.")
  private List<AbstractLevelDTO> levels = new ArrayList<>();
}
