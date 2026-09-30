package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** The type Assessment answers */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class AssessmentAnswered extends AbstractAuditPOJO {

  public static final String TYPE = "assessment_answered";

  /** Typed per-question answers submitted by the trainee, one carrier per answered question */
  @JsonProperty("answers")
  private List<EventAnswer> answers;
}
