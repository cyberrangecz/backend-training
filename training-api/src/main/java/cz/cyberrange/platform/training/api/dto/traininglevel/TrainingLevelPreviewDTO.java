package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.hint.TakenHintDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A previously visited level replayed back to the trainee who visited it, as part of a training
 * run.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "TrainingLevelPreviewDTO",
    description = "An assignment containing security tasks whose completion yields a answer.",
    parent = AbstractLevelDTO.class)
public class TrainingLevelPreviewDTO extends AbstractLevelDTO {

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Play me")
  private String content;

  /** Hints this trainee actually took while visiting the level, rebuilt from their run history. */
  @ApiModelProperty(value = "Information which helps player resolve the level.")
  private Set<TakenHintDTO> hints = new HashSet<>();

  /**
   * The solution as recorded for this trainee's run, with any embedded answer placeholder resolved
   * to their own answer; null if this trainee has not displayed it.
   */
  @ApiModelProperty(
      value = "Instruction how to get answer in training.",
      example = "This is how you do it")
  private String solution;
}
