package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wrong answer submitted event, carrying the {@code type} value {@code wrong_answer_submitted}.
 * Recorded for a training level's flag-style answer, distinct from an assessment question answer
 * carried by {@link AssessmentAnsweredDTO}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Records that a trainee submitted a wrong answer for a training level.")
public class WrongAnswerSubmittedDTO extends TrainingEventDTO {

  /** The training level answer text the trainee submitted */
  @JsonProperty("answer_content")
  private String answerContent;

  /**
   * Running count of wrong submissions made so far in the current level, copied from {@code
   * TrainingRun.incorrectAnswerCount} at the time this event was recorded; not a count of attempts
   * remaining
   */
  @JsonProperty("count")
  private Long count;
}
