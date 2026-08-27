package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "WrongAnswerSubmittedDTO", description = "Wrong answer submitted event")
public class WrongAnswerSubmittedDTO extends TrainingEventDTO {

  /** The training level answer text the trainee submitted */
  @ApiModelProperty(value = "Answer content")
  @JsonProperty("answer_content")
  private String answerContent;

  /**
   * Running count of wrong submissions made so far in the current level, copied from {@code
   * TrainingRun.incorrectAnswerCount} at the time this event was recorded; not a count of attempts
   * remaining
   */
  @ApiModelProperty(value = "Attempt count")
  @JsonProperty("count")
  private Long count;
}
