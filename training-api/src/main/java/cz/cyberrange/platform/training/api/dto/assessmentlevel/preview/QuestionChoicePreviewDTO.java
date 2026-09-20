package cz.cyberrange.platform.training.api.dto.assessmentlevel.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;

/**
 * A question choice as shown to a participant; it has no correctness flag, so it never discloses
 * which choice is correct
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "One answer choice as shown to a participant, with no correctness marking.")
public class QuestionChoicePreviewDTO implements Serializable, Ordered {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "Yes")
  private String text;

  @Schema(description = "Zero-based position of the choice in its question.", example = "1")
  private int order;
}
