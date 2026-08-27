package cz.cyberrange.platform.training.opensearch.events.training.model.enums;

/**
 * Category of a level, carried as {@code level_type} on level lifecycle events. Resolved from the
 * concrete class of the training run's current level.
 */
public enum EventLevelType {
  /** The current level is an {@code InfoLevel} */
  INFO,
  /** The current level is an {@code AssessmentLevel} */
  ASSESSMENT,
  /** The current level is a {@code TrainingLevel} */
  TRAINING,
  /** The current level is an {@code AccessLevel} */
  ACCESS,
  /** Used when the current level matches none of the other level classes */
  PVP
}
