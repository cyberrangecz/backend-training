package cz.cyberrange.platform.training.opensearch.events.training.query.projection;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.Map;
import java.util.Set;

/**
 * What the audit index holds about one training run: where its score stood, what it last scored on
 * each level, and how often it produced each of the event types the caller asked to be counted.
 *
 * <p>Absence is reported as absence. A run with no events at all carries a null snapshot, and a
 * level or event type that never occurred has no entry rather than a zero.
 *
 * @param latestScore the run's standing at its most recent event, or null when it produced none.
 * @param scoreByLevelId score at the most recent completion of each level, keyed by level id.
 * @param countByEventTypeAndLevelId occurrence count keyed by level id, nested under event type.
 */
public record RunEventAggregate(
    RunScoreSnapshot latestScore,
    Map<Long, Integer> scoreByLevelId,
    Map<String, Map<Long, Long>> countByEventTypeAndLevelId) {

  public RunEventAggregate {
    scoreByLevelId = Map.copyOf(scoreByLevelId);
    countByEventTypeAndLevelId =
        countByEventTypeAndLevelId.entrySet().stream()
            .collect(toUnmodifiableMap(Map.Entry::getKey, entry -> Map.copyOf(entry.getValue())));
  }

  /**
   * Total occurrences of an event type across every level of the run.
   *
   * @param eventType the type to total.
   * @return the count, zero when the type never occurred.
   */
  public long countOf(String eventType) {
    return countByEventTypeAndLevelId.getOrDefault(eventType, Map.of()).values().stream()
        .mapToLong(Long::longValue)
        .sum();
  }

  /**
   * Occurrences of an event type, excluding those recorded on the given levels.
   *
   * @param eventType the type to total.
   * @param excludedLevelIds levels whose occurrences do not count.
   * @return the count, zero when the type never occurred outside those levels.
   */
  public long countOfExcludingLevels(String eventType, Set<Long> excludedLevelIds) {
    return countByEventTypeAndLevelId.getOrDefault(eventType, Map.of()).entrySet().stream()
        .filter(entry -> !excludedLevelIds.contains(entry.getKey()))
        .mapToLong(Map.Entry::getValue)
        .sum();
  }
}
