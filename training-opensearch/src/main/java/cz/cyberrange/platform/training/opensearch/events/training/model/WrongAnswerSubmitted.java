package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Records a submitted answer that did not match a training level's correct answer, or a submitted
 * access-level passkey, carried under the {@code wrong_answer_submitted} type
 */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class WrongAnswerSubmitted extends AbstractAuditPOJO {

  public static final String TYPE = "wrong_answer_submitted";

  /** The submitted text or passkey */
  @JsonProperty(value = "answer_content", required = true)
  @JsonAlias("flag_content")
  private String answerContent;

  /** Trainee's incorrect-answer count in the current level as of this event */
  @JsonProperty(value = "count", required = true)
  private int count;
}
