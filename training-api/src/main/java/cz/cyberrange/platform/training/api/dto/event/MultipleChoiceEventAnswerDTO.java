package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Multiple-choice assessment answer holding the options the trainee selected, ordered by option
 * order, each carrying whether that individual option is a correct choice.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString(callSuper = true)
@ApiModel(value = "MultipleChoiceEventAnswerDTO", description = "Multiple-choice assessment answer")
public class MultipleChoiceEventAnswerDTO extends EventAnswerDTO {

  public static final String TYPE = "MCQ";

  @ApiModelProperty(value = "Selected options ordered by option order, each with its correctness")
  @JsonProperty("selected_options")
  private List<AnswerSelectionDTO<Integer>> selectedOptions;
}
