package cz.cyberrange.platform.training.api.dto.export;

import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about assessment level. Inherits from {@link AbstractLevelExportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An exported assessment level and the questions it asks")
public class AssessmentLevelExportDTO extends AbstractLevelExportDTO {

  /**
   * Each question and its nested choices, extended-matching statements and options carry no id;
   * only their own data is exported
   */
  @Schema(description = "Exported without ids, each question carrying its own data only")
  private List<QuestionDTO> questions;

  @Schema(example = "Fill me up")
  private String instructions;

  @Schema(example = "TEST")
  private AssessmentType assessmentType;
}
