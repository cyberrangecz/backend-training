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

/** Records a level being finished, carried under the {@code level_completed} type. */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class LevelCompleted extends AbstractAuditPOJO {

  public static final String TYPE = "level_completed";

  /** Category of the level that was completed. */
  @JsonProperty(value = "level_type", required = true)
  private EventLevelType levelType;
}
