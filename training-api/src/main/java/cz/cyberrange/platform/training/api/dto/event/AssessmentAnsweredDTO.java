package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Assessment answered event, carrying the {@code type} value {@code assessment_answered} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "AssessmentAnsweredDTO", description = "Assessment answered event")
public class AssessmentAnsweredDTO extends TrainingEventDTO {

  @ApiModelProperty(value = "Typed per-question answers submitted by the trainee")
  @JsonProperty("answers")
  private List<EventAnswerDTO> answers;
}
