package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Records a trainee revealing a training level's solution, carried under the {@code
 * solution_displayed} type.
 */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class SolutionDisplayed extends AbstractAuditPOJO {

  public static final String TYPE = "solution_displayed";

  /** The level's remaining score after the solution penalty, if any, is applied. */
  @JsonProperty(value = "penalty_points", required = true)
  private int penaltyPoints;
}
