package cz.cyberrange.platform.training.api.dto.export;

import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about assessment level. Inherits from {@link AbstractLevelExportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "AssessmentLevelExportDTO",
    description = "Exported assessment level.",
    parent = AbstractLevelExportDTO.class)
public class AssessmentLevelExportDTO extends AbstractLevelExportDTO {

  /**
   * Each question and its nested choices, extended-matching statements and options carry no id;
   * only their own data is exported
   */
  @ApiModelProperty(
      value = "List of questions in this assessment as JSON.",
      example = "What is my mothers name?")
  private List<QuestionDTO> questions;

  @ApiModelProperty(value = "Assessment instructions for participant.", example = "Fill me up")
  private String instructions;

  @ApiModelProperty(value = "Type of assessment.", example = "TEST")
  private AssessmentType assessmentType;
}
