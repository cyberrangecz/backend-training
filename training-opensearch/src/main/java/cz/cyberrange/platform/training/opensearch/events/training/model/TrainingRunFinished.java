package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** Records a training run ending, carried under the {@code training_run_finished} type */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class TrainingRunFinished extends AbstractAuditPOJO {

  public static final String TYPE = "training_run_finished";

  /** Epoch-millisecond instant the training run started */
  @JsonProperty(value = "start_time", required = true)
  private long startTime;

  /** Epoch-millisecond instant the training run finished */
  @JsonProperty(value = "end_time", required = true)
  private long endTime;
}
