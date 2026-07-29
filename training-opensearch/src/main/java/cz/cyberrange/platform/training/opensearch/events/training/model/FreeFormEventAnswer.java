package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Free-form assessment answer holding the trainee's submitted text together with whether that text
 * is correct.
 */
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class FreeFormEventAnswer extends EventAnswer {

  public static final String TYPE = "FFQ";

  @JsonProperty("answer")
  private AnswerSelection<String> answer;
}
