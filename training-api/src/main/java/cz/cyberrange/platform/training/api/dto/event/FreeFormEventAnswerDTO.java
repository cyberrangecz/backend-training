package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Free-form assessment answer holding the trainee's submitted text together with whether that text
 * is correct.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString(callSuper = true)
@ApiModel(value = "FreeFormEventAnswerDTO", description = "Free-form assessment answer")
public class FreeFormEventAnswerDTO extends EventAnswerDTO {

  public static final String TYPE = "FFQ";

  @ApiModelProperty(value = "Submitted free-form answer text with its correctness")
  @JsonProperty("answer")
  private AnswerSelectionDTO<String> answer;
}
