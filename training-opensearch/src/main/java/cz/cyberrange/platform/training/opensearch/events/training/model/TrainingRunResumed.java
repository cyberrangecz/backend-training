package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Records a trainee resuming an in-progress training run, carried under the {@code
 * training_run_resumed} type
 */
@SuperBuilder
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class TrainingRunResumed extends AbstractAuditPOJO {
  public static final String TYPE = "training_run_resumed";
}
