package cz.cyberrange.platform.training.service.services.score;

import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/** The span a training run occupied, measured against the lifetime of the instance hosting it */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RunWindow {

  /** Whether the run has ended, by its own state or because its instance has */
  private final boolean finished;

  /** Run start, in epoch milliseconds */
  private final long startMillis;

  /** Run end capped at the instance end, in epoch milliseconds; null while the run is going */
  private final Long endMillis;

  /**
   * Length of the span in milliseconds, never negative, measured to the current instant while the
   * run is still going so that it can be ranked
   */
  private final long elapsedMillis;

  /**
   * Resolves the span of a run. A run counts as finished once its own state says so or once its
   * instance has ended, whichever comes first, since a run cannot outlive the instance hosting it;
   * a finished run's end is cut back to the instance end when it falls beyond it. A run still going
   * is measured to {@code nowMillis} so that it can be ranked against the others, but reports no
   * end of its own.
   *
   * @param run the run to measure.
   * @param instance the instance hosting the run.
   * @param nowMillis the current instant in epoch milliseconds, shared by every run of one report
   *     so that they are measured against a single reference.
   * @return the resolved span.
   */
  public static RunWindow resolve(TrainingRun run, TrainingInstance instance, long nowMillis) {
    long startMillis = run.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
    long instanceEndMillis = instance.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli();
    boolean finished = nowMillis > instanceEndMillis || run.getState() != TRState.RUNNING;
    if (!finished) {
      return new RunWindow(false, startMillis, null, Math.max(0, nowMillis - startMillis));
    }
    long endMillis =
        Math.min(run.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli(), instanceEndMillis);
    return new RunWindow(true, startMillis, endMillis, Math.max(0, endMillis - startMillis));
  }

  /**
   * Length of the run in whole seconds, rounded half up.
   *
   * @return the length, or null while the run is still going.
   */
  public Long getDurationSeconds() {
    if (!finished) {
      return null;
    }
    return Math.round(elapsedMillis / 1000.0);
  }
}
