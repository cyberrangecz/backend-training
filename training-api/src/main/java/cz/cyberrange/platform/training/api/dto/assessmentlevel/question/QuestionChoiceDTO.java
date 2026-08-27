package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionChoiceDTO implements Serializable, Ordered {

  @ApiModelProperty(value = "Main identifier of the question choice.", example = "1")
  private Long id;

  @ApiModelProperty(value = "Text content of the question choice.", example = "Yes")
  @NotEmpty(message = "{questionChoice.text.NotEmpty.message}")
  private String text;

  /**
   * Whether this choice is a correct answer. Consulted only when scoring a multiple-choice
   * question, where every submitted answer must equal the text of exactly the choices marked
   * correct; ignored for a free-form question, whose choices are all treated as accepted answer
   * texts regardless of this flag. Cleared to null before an assessment level reaches a trainee's
   * current level in a training run, but left set when a designer retrieves the level for editing.
   */
  @ApiModelProperty(value = "Sign if the choice is correct.", example = "true")
  @NotNull(message = "{questionChoice.correct.NotNull.message}")
  private Boolean correct;

  @ApiModelProperty(value = "The order of the choice in question of type MCQ or FFQ", example = "1")
  @Min(value = 0, message = "{questionChoice.order.Min.message}")
  private int order;
}
