package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * Reports one level's identity and correct answers, built by hand in {@code TrainingRunFacade} from
 * a {@code TrainingLevel} or a test {@code AssessmentLevel} entity and returned in bulk from the
 * training run's answers endpoint. A training level carries {@code correctAnswer}, an assessment
 * level {@code questionCorrectAnswers}; the class-level {@code @JsonInclude} omits whatever is
 * unset, such as {@code variableName} of a level with no variant answers.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "One level's correct answers, resolved for the asking participant.")
public class CorrectAnswerDTO {

  @Schema(example = "1")
  private Long levelId;

  @Schema(example = "Training Level1")
  private String levelTitle;

  @Schema(description = "Zero-based position of the level within its definition.", example = "2")
  private Integer levelOrder;

  @Schema(description = "TRAINING_LEVEL or ASSESSMENT_LEVEL.", example = "TRAINING_LEVEL")
  private LevelType levelType;

  /**
   * Holds the level's static answer, or, for a level using variant answers, whatever value the
   * variant-answer lookup resolved for it, which may be null when that lookup found none
   */
  @Schema(
      description = "The level's answer, resolved for this participant when answers vary.",
      example = "john")
  private String correctAnswer;

  /**
   * Names the answer variable the level was configured with; unset when the level uses a single
   * static answer
   */
  @Schema(
      description = "The answer variable the level uses; absent for a fixed answer.",
      example = "username")
  private String variableName;

  /** The correct answers of each question of an assessment level, in question order */
  @Schema(description = "The correct answers of each question of an assessment level.")
  private List<QuestionCorrectAnswerDTO> questionCorrectAnswers;
}
