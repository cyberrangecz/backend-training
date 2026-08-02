package cz.cyberrange.platform.training.opensearch.events.training.query.projection;

/**
 * The score a training run stood at when its most recent event was written. Every training event
 * carries the run's cumulative totals, so the latest one states the run's standing.
 *
 * @param totalTrainingScore cumulative score across the run's training levels.
 * @param totalAssessmentScore cumulative score across the run's assessment levels.
 */
public record RunScoreSnapshot(int totalTrainingScore, int totalAssessmentScore) {}
