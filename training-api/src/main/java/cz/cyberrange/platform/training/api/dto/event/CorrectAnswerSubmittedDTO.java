package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Correct answer submitted event, carrying the {@code type} value {@code correct_answer_submitted}.
 * Recorded for a training level's flag-style answer, distinct from an assessment question answer
 * carried by {@link AssessmentAnsweredDTO}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Records that a trainee submitted the right answer for a training level.")
public class CorrectAnswerSubmittedDTO extends TrainingEventDTO {

  /** The training level answer text the trainee submitted */
  @JsonProperty("answer_content")
  private String answerContent;
}
