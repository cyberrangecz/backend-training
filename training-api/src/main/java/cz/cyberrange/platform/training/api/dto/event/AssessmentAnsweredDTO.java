package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Assessment answered event, carrying the {@code type} value {@code assessment_answered} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Records that a trainee answered the questions of an assessment level.")
public class AssessmentAnsweredDTO extends TrainingEventDTO {

  @JsonProperty("answers")
  private List<EventAnswerDTO> answers;
}
