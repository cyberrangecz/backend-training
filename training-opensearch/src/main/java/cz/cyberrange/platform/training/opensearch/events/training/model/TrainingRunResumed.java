package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** The type Training run resumed. */
@SuperBuilder
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class TrainingRunResumed extends AbstractAuditPOJO {
  public static final String TYPE = "training_run_resumed";
}
