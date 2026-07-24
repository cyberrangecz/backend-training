package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Extended-matching assessment answer holding, per statement order, the option the trainee matched
 * to it together with whether that individual pairing is correct.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString(callSuper = true)
@ApiModel(
    value = "ExtendedMatchingEventAnswerDTO",
    description = "Extended-matching assessment answer")
public class ExtendedMatchingEventAnswerDTO extends EventAnswerDTO {

  public static final String TYPE = "EMI";

  @ApiModelProperty(value = "Statement order mapped to the matched option with its correctness")
  @JsonProperty("pairs")
  private Map<Integer, AnswerSelectionDTO<Integer>> pairs;
}
