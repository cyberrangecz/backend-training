package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * A single value the trainee selected for an assessment question together with whether that
 * individual selection is correct. Used uniformly across question types: the free-form answer text,
 * one selected multiple-choice option order, or one matched extended-matching option order.
 *
 * @param <T> type of the selected value; the answer text for free-form answers, the option order
 *     for multiple-choice and extended-matching answers
 */
@Data
@Schema(description = "One value a trainee chose for an assessment question.")
public class AnswerSelectionDTO<T> {

  @Schema(
      example = "2",
      description = "The typed text for a free-form answer, otherwise the chosen option order.")
  @JsonProperty("value")
  private T value;

  @Schema(example = "true", description = "Whether this one selection is correct.")
  @JsonProperty("correct")
  private Boolean correct;
}
