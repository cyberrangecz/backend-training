package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Extended-matching assessment answer holding, per statement order, the option the trainee matched
 * to it together with whether that individual pairing is correct
 */
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ExtendedMatchingEventAnswer extends EventAnswer {

  public static final String TYPE = "EMI";

  @JsonProperty("pairs")
  private Map<Integer, AnswerSelection<Integer>> pairs;
}
