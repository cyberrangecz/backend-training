package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** Records a training run starting, carried under the {@code training_run_started} type. */
@SuperBuilder
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class TrainingRunStarted extends AbstractAuditPOJO {
  public static final String TYPE = "training_run_started";
}
