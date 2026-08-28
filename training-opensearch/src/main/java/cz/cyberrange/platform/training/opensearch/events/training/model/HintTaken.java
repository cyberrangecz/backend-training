package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** Records a hint the trainee revealed during a level, carried under the {@code hint_taken} type */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class HintTaken extends AbstractAuditPOJO {

  public static final String TYPE = "hint_taken";

  /** Primary key of the hint that was revealed */
  @JsonProperty(value = "hint_id", required = true)
  private long hintId;

  /** Points deducted from the level score for revealing the hint */
  @JsonProperty(value = "hint_penalty_points", required = true)
  private int hintPenaltyPoints;

  /** Title of the hint that was revealed */
  @JsonProperty(value = "hint_title", required = true)
  private String hintTitle;
}
