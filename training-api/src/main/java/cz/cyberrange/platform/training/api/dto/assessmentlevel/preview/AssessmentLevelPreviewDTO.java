package cz.cyberrange.platform.training.api.dto.assessmentlevel.preview;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An assessment level as shown for a level the participant has already visited, carrying the
 * participant's own submitted answers but never disclosing which choice or option was correct
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An assessment level replayed with the participant's own answers.")
public class AssessmentLevelPreviewDTO extends AbstractLevelDTO {

  @Schema(description = "The level's questions, each with the participant's answers.")
  private List<QuestionPreviewDTO> questions;

  @Schema(example = "Fill me up")
  private String instructions;

  @Schema(
      description = "Whether the answers are graded (TEST) or only collected (QUESTIONNAIRE).",
      example = "TEST")
  private AssessmentType assessmentType;
}
