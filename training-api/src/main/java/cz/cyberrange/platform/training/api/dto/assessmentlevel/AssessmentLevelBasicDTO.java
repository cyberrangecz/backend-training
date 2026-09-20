package cz.cyberrange.platform.training.api.dto.assessmentlevel;

import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionBasicDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Summary of an assessment level returned when looking up training definitions by id in bulk; its
 * questions carry only scoring and type information, never answer choices or correctness data
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An assessment level in outline, its questions carrying only scoring.")
public class AssessmentLevelBasicDTO
    extends cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO {

  @Schema(
      description = "Whether the answers are graded (TEST) or only collected (QUESTIONNAIRE).",
      example = "TEST")
  protected AssessmentType assessmentType;

  protected List<QuestionBasicDTO> questions;
}
