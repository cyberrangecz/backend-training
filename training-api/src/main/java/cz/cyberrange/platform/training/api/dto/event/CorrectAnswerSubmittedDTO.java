package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "CorrectAnswerSubmittedDTO", description = "Correct answer submitted event")
public class CorrectAnswerSubmittedDTO extends TrainingEventDTO {

  /** The training level answer text the trainee submitted */
  @ApiModelProperty(value = "Answer content")
  @JsonProperty("answer_content")
  private String answerContent;
}
