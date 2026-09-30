package cz.cyberrange.platform.training.api.dto.assessmentlevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** A level that poses questions the participant must answer; it may carry none */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An assessment level with the full detail of its questions.")
public class AssessmentLevelDTO extends AbstractLevelDTO {

  @Schema(description = "The level's questions, each with its answer options.")
  private List<QuestionDTO> questions;

  @Schema(example = "Fill me up")
  private String instructions;

  @Schema(
      description = "Whether the answers are graded (TEST) or only collected (QUESTIONNAIRE).",
      example = "TEST")
  private AssessmentType assessmentType;

  /**
   * Threshold, in minutes, below which a participant's time spent on the level is flagged as an
   * unusually fast solve by cheating detection
   */
  @Schema(
      description = "Time in minutes below which a solve is flagged as suspiciously fast.",
      example = "5")
  private Integer minimalPossibleSolveTime;
}
