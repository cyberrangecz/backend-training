package cz.cyberrange.platform.training.persistence.model.enums;

/**
 * How far a cheating detection has got, tracked both for the run as a whole and separately for each
 * kind of detection within it. Stored by name.
 */
public enum CheatingDetectionState {

  /** Accepted and waiting to be executed. */
  QUEUED,
  /** Executing now. */
  RUNNING,
  /**
   * Excluded from the run. A detection asked for in this state is left in it rather than queued, so
   * it never executes.
   */
  DISABLED,
  /** Executed to completion; any findings it made are recorded. */
  FINISHED;
}
