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

/** The type Level started. */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class LevelStarted extends AbstractAuditPOJO {

  public static final String TYPE = "level_started";

  @JsonProperty(value = "level_type", required = true)
  private EventLevelType levelType;

  @JsonProperty(value = "max_score", required = true)
  private int maxScore;

  @JsonProperty(value = "level_title", required = true)
  private String levelTitle;
}
