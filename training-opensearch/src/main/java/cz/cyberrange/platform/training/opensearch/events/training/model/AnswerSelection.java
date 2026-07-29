package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single value the trainee selected for an assessment question together with whether that
 * individual selection is correct. Used uniformly across question types: the free-form answer text,
 * one selected multiple-choice option order, or one matched extended-matching option order.
 *
 * @param <T> type of the selected value; the answer text for free-form answers, the option order
 *     for multiple-choice and extended-matching answers
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AnswerSelection<T> {

  @JsonProperty("value")
  private T value;

  @JsonProperty("correct")
  private Boolean correct;
}
