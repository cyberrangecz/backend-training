package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Multiple-choice assessment answer holding the options the trainee selected, ordered by option
 * order, each carrying whether that individual option is a correct choice.
 */
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class MultipleChoiceEventAnswer extends EventAnswer {

  public static final String TYPE = "MCQ";

  @JsonProperty("selected_options")
  private List<AnswerSelection<Integer>> selectedOptions;
}
