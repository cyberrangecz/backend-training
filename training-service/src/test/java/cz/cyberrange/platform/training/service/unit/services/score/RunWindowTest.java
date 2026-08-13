package cz.cyberrange.platform.training.service.unit.services.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.service.services.score.RunWindow;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RunWindow}.
 *
 * <p>Covers span resolution: finished-state determination from the run's own state and from the
 * instance lifetime, end-time capping at the instance end, and whole-second duration rounding.
 */
@DisplayName("RunWindow")
class RunWindowTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 0, 0, 0);

  private static long toEpochMillis(LocalDateTime dateTime) {
    return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
  }

  private TrainingRun run(LocalDateTime start, LocalDateTime end, TRState state) {
    TrainingRun run = new TrainingRun();
    run.setStartTime(start);
    run.setEndTime(end);
    run.setState(state);
    return run;
  }

  private TrainingInstance instance(LocalDateTime end) {
    TrainingInstance instance = new TrainingInstance();
    instance.setEndTime(end);
    return instance;
  }

  @Test
  @DisplayName(
      "a running run under an instance that has not ended reports no end but an elapsed time")
  void runningRunUnderUnendedInstanceReportsElapsedButNoEnd() {
    TrainingRun run = run(START, START.plusSeconds(10), TRState.RUNNING);
    TrainingInstance instance = instance(START.plusHours(1));
    long nowMillis = toEpochMillis(START.plusSeconds(3));

    RunWindow window = RunWindow.resolve(run, instance, nowMillis);

    assertFalse(window.isFinished());
    assertNull(window.getEndMillis());
    assertNull(window.getDurationSeconds());
    assertEquals(3000L, window.getElapsedMillis());
  }

  @Test
  @DisplayName("a finished run ending before the instance end keeps its own end and duration")
  void finishedRunEndingBeforeInstanceEndUsesOwnEnd() {
    LocalDateTime end = START.plusSeconds(5);
    TrainingRun run = run(START, end, TRState.FINISHED);
    TrainingInstance instance = instance(START.plusSeconds(20));
    long nowMillis = toEpochMillis(START.plusHours(1));

    RunWindow window = RunWindow.resolve(run, instance, nowMillis);

    assertTrue(window.isFinished());
    assertEquals(toEpochMillis(end), window.getEndMillis());
    assertEquals(5L, window.getDurationSeconds());
  }

  @Test
  @DisplayName("a finished run outlasting its instance is cut at the instance end")
  void finishedRunOutlastingInstanceIsCutAtInstanceEnd() {
    TrainingRun run = run(START, START.plusSeconds(30), TRState.FINISHED);
    TrainingInstance instance = instance(START.plusSeconds(20));
    long nowMillis = toEpochMillis(START.plusHours(1));

    RunWindow window = RunWindow.resolve(run, instance, nowMillis);

    assertTrue(window.isFinished());
    assertEquals(toEpochMillis(START.plusSeconds(20)), window.getEndMillis());
    assertEquals(20L, window.getDurationSeconds());
  }

  @Test
  @DisplayName("an archived run counts as finished even though its instance has not ended")
  void archivedRunCountsAsFinished() {
    TrainingRun run = run(START, START.plusSeconds(2), TRState.ARCHIVED);
    TrainingInstance instance = instance(START.plusHours(10));
    long nowMillis = toEpochMillis(START.plusSeconds(4));

    RunWindow window = RunWindow.resolve(run, instance, nowMillis);

    assertTrue(window.isFinished());
    assertEquals(toEpochMillis(START.plusSeconds(2)), window.getEndMillis());
    assertEquals(2L, window.getDurationSeconds());
  }

  @Test
  @DisplayName("an ended instance ends every run still running under it, at the instance end")
  void endedInstanceEndsRunningRunAtInstanceEnd() {
    LocalDateTime instanceEnd = START.plusSeconds(10);
    TrainingRun run = run(START, instanceEnd, TRState.RUNNING);
    TrainingInstance instance = instance(instanceEnd);
    long nowMillis = toEpochMillis(START.plusSeconds(15));

    RunWindow window = RunWindow.resolve(run, instance, nowMillis);

    assertTrue(window.isFinished());
    assertEquals(toEpochMillis(instanceEnd), window.getEndMillis());
    assertEquals(10L, window.getDurationSeconds());
  }

  @Test
  @DisplayName(
      "a run starting after its instance ended collapses to zero duration rather than negative")
  void runStartingAfterInstanceEndCollapsesToZero() {
    LocalDateTime instanceEnd = START.plusSeconds(10);
    TrainingRun run =
        run(instanceEnd.plusSeconds(20), instanceEnd.plusSeconds(25), TRState.FINISHED);
    TrainingInstance instance = instance(instanceEnd);
    long nowMillis = toEpochMillis(instanceEnd.plusHours(1));

    RunWindow window = RunWindow.resolve(run, instance, nowMillis);

    assertEquals(0L, window.getElapsedMillis());
    assertEquals(0L, window.getDurationSeconds());
  }

  @Test
  @DisplayName("duration rounds to the nearest whole second, half up")
  void durationRoundsHalfUp() {
    TrainingRun run = run(START, START.plusNanos(1_500_000_000L), TRState.FINISHED);
    TrainingInstance instance = instance(START.plusHours(1));
    long nowMillis = toEpochMillis(START.plusHours(1));

    RunWindow window = RunWindow.resolve(run, instance, nowMillis);

    assertEquals(2L, window.getDurationSeconds());
  }

  @Test
  @DisplayName("a run ending exactly at the instance end keeps its own end uncut")
  void runEndingExactlyAtInstanceEndKeepsOwnEnd() {
    LocalDateTime boundary = START.plusSeconds(10);
    TrainingRun run = run(START, boundary, TRState.FINISHED);
    TrainingInstance instance = instance(boundary);
    long nowMillis = toEpochMillis(START.plusHours(1));

    RunWindow window = RunWindow.resolve(run, instance, nowMillis);

    assertTrue(window.isFinished());
    assertEquals(toEpochMillis(boundary), window.getEndMillis());
    assertEquals(10L, window.getDurationSeconds());
  }
}
