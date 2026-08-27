package cz.cyberrange.platform.training.api.enums;

/**
 * Classifies which action is offered next for a training run that has already been accessed.
 * Resolved for each run and carried on its accessed-run API representation.
 */
public enum Actions {
  NONE,
  /** Chosen when the run has finished or its training instance's end time has passed. */
  RESULTS,

  /** Chosen when the run has not finished and its training instance is still running. */
  RESUME;
}
