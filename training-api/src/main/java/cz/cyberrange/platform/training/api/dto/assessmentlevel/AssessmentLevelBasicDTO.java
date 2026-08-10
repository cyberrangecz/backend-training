package cz.cyberrange.platform.training.api.dto.assessmentlevel;

import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionBasicDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@ApiModel(
    value = "AssessmentLevelBasicDTO",
    description = "A questionnaire or a test that is displayed to the participant.",
    parent = cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO.class)
public class AssessmentLevelBasicDTO
    extends cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO {

  @ApiModelProperty(value = "Type of assessment.", example = "TEST")
  protected AssessmentType assessmentType;

  @ApiModelProperty("List of questions in this assessment")
  protected List<QuestionBasicDTO> questions;
}
