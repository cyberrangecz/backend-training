package cz.cyberrange.platform.training.service.unit.services.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cyberrange.platform.training.opensearch.events.training.query.projection.RunEventAggregate;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RunEventAggregate}.
 *
 * <p>Covers the counting helpers: totalling occurrences of an event type across levels, and
 * totalling while excluding specified levels, including their zero-when-absent behaviour.
 */
@DisplayName("RunEventAggregate")
class RunEventAggregateTest {

  private static final String HINT_TAKEN = "hint_taken";
  private static final String WRONG_ANSWER_SUBMITTED = "wrong_answer_submitted";

  @Test
  @DisplayName("countOf sums occurrences of an event type across every level")
  void countOfSumsAcrossLevels() {
    RunEventAggregate aggregate =
        new RunEventAggregate(
            null, Map.of(), Map.of(WRONG_ANSWER_SUBMITTED, Map.of(1L, 2L, 2L, 3L)));

    assertEquals(5L, aggregate.countOf(WRONG_ANSWER_SUBMITTED));
  }

  @Test
  @DisplayName("countOf reads as zero when the event type never occurred")
  void countOfReturnsZeroWhenEventTypeNeverOccurred() {
    RunEventAggregate aggregate = new RunEventAggregate(null, Map.of(), Map.of());

    assertEquals(0L, aggregate.countOf(HINT_TAKEN));
  }

  @Test
  @DisplayName("countOfExcludingLevels omits occurrences recorded on the excluded levels")
  void countOfExcludingLevelsOmitsExcludedLevels() {
    RunEventAggregate aggregate =
        new RunEventAggregate(
            null, Map.of(), Map.of(WRONG_ANSWER_SUBMITTED, Map.of(1L, 2L, 2L, 3L)));

    long count = aggregate.countOfExcludingLevels(WRONG_ANSWER_SUBMITTED, Set.of(2L));

    assertEquals(2L, count);
  }

  @Test
  @DisplayName("countOfExcludingLevels reads as zero when only excluded levels produced the type")
  void countOfExcludingLevelsReturnsZeroWhenOnlyExcludedLevelsProducedType() {
    RunEventAggregate aggregate =
        new RunEventAggregate(null, Map.of(), Map.of(WRONG_ANSWER_SUBMITTED, Map.of(2L, 3L)));

    long count = aggregate.countOfExcludingLevels(WRONG_ANSWER_SUBMITTED, Set.of(2L));

    assertEquals(0L, count);
  }
}
