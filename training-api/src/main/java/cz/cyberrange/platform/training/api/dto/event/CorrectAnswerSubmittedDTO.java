package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "CorrectAnswerSubmittedDTO", description = "Correct answer submitted event")
public class CorrectAnswerSubmittedDTO extends TrainingEventDTO {

  @ApiModelProperty(value = "Answer content")
  @JsonProperty("answer_content")
  private String answerContent;
}
