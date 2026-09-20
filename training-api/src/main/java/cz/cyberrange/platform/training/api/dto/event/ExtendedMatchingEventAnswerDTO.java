package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Extended-matching assessment answer holding, per statement order, the option the trainee matched
 * to it together with whether that individual pairing is correct
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "The option a trainee matched to each statement of a question.")
public class ExtendedMatchingEventAnswerDTO extends EventAnswerDTO {

  public static final String TYPE = "EMI";

  @JsonProperty("pairs")
  private Map<Integer, AnswerSelectionDTO<Integer>> pairs;
}
