package cz.cyberrange.platform.training.api.dto.assessmentlevel.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

/**
 * A question choice as shown to a participant; it has no correctness flag, so it never discloses
 * which choice is correct
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionChoicePreviewDTO implements Serializable, Ordered {

  @ApiModelProperty(value = "Main identifier of the question choice.", example = "1")
  private Long id;

  @ApiModelProperty(value = "Text content of the question choice.", example = "Yes")
  private String text;

  @ApiModelProperty(value = "The order of the choice in question of type MCQ or FFQ", example = "1")
  private int order;
}
