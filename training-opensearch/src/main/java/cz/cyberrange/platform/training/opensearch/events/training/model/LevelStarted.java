package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.cyberrange.platform.training.opensearch.events.training.model.enums.EventLevelType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Records a level starting, whether as the first level of a training run or after advancing from
 * the previous one, carried under the {@code level_started} type.
 */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class LevelStarted extends AbstractAuditPOJO {

  public static final String TYPE = "level_started";

  /** Category of the level that was started. */
  @JsonProperty(value = "level_type", required = true)
  private EventLevelType levelType;

  /** Highest score obtainable in the level. */
  @JsonProperty(value = "max_score", required = true)
  private int maxScore;

  /** Title of the level that was started. */
  @JsonProperty(value = "level_title", required = true)
  private String levelTitle;
}
