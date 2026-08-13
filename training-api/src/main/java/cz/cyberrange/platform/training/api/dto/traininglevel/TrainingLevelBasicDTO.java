package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintBasicDTO;
import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "TrainingLevelBasicDTO",
    description = "An assignment containing security tasks whose completion yields a answer.",
    parent = AbstractLevelBasicDTO.class)
public class TrainingLevelBasicDTO extends AbstractLevelBasicDTO {
  @ApiModelProperty(value = "Information which helps player resolve the level.")
  protected Set<HintBasicDTO> hints = new HashSet<>();

  @ApiModelProperty(
      value = "How many times player can submit incorrect answer before displaying solution.",
      example = "5")
  protected int incorrectAnswerLimit;

  @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
  protected boolean solutionPenalized;

  @ApiModelProperty(value = "List of mitre techniques used in the training level.")
  protected List<MitreTechniqueDTO> mitreTechniques;
}
