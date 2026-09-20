package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Free-form assessment answer holding the trainee's submitted text together with whether that text
 * is correct
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "The text a trainee typed for a free-form question.")
public class FreeFormEventAnswerDTO extends EventAnswerDTO {

  public static final String TYPE = "FFQ";

  @JsonProperty("answer")
  private AnswerSelectionDTO<String> answer;
}
