package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A single value the trainee selected for an assessment question together with whether that
 * individual selection is correct. Used uniformly across question types: the free-form answer text,
 * one selected multiple-choice option order, or one matched extended-matching option order.
 *
 * @param <T> type of the selected value; the answer text for free-form answers, the option order
 *     for multiple-choice and extended-matching answers
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(
    value = "AnswerSelectionDTO",
    description = "A submitted value together with whether that selection is correct")
public class AnswerSelectionDTO<T> {

  @ApiModelProperty(value = "The submitted value", example = "2")
  @JsonProperty("value")
  private T value;

  @ApiModelProperty(
      value = "Whether this specific selection is correct; null when the assessment is not scored",
      example = "true")
  @JsonProperty("correct")
  private Boolean correct;
}
