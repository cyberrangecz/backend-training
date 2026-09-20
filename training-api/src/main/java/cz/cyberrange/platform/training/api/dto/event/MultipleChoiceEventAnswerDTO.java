package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Multiple-choice assessment answer holding the options the trainee selected, ordered by option
 * order, each carrying whether that individual option is a correct choice
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "The options a trainee chose for a multiple choice question.")
public class MultipleChoiceEventAnswerDTO extends EventAnswerDTO {

  public static final String TYPE = "MCQ";

  @JsonProperty("selected_options")
  private List<AnswerSelectionDTO<Integer>> selectedOptions;
}
