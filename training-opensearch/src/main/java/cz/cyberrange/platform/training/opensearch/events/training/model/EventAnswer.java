package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base type for a single trainee answer to one assessment question, carried by the {@code
 * assessment_answered} event. Holds the answered question reference together with the answer's
 * correctness and net points gained; each concrete subtype adds the answer value in the shape
 * appropriate to its question type. Correctness and points are {@code null} when the assessment is
 * not scored. The {@code type} property discriminates the subtype during serialization and
 * deserialization.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = FreeFormEventAnswer.class, name = FreeFormEventAnswer.TYPE),
  @JsonSubTypes.Type(
      value = MultipleChoiceEventAnswer.class,
      name = MultipleChoiceEventAnswer.TYPE),
  @JsonSubTypes.Type(
      value = ExtendedMatchingEventAnswer.class,
      name = ExtendedMatchingEventAnswer.TYPE),
})
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public abstract class EventAnswer {

  @JsonProperty("question_id")
  protected Long questionId;

  @JsonProperty("correct")
  protected Boolean correct;

  @JsonProperty("points_gained")
  protected Integer pointsGained;
}
