package cz.cyberrange.platform.training.api.enums;

/**
 * Classifies the progress of a cheating detection run, tracked both for the run as a whole and
 * separately for each detection method it executes.
 */
public enum CheatingDetectionState {
  /** Set on a method before it executes; checked by the method's routine to decide to run it. */
  QUEUED,
  /**
   * Set on the run while its selected methods execute, and on a method's own state while that
   * method's routine is executing.
   */
  RUNNING,
  /** Marks a method excluded from execution; preserved across re-execution rather than requeued. */
  DISABLED,
  /**
   * Set on the run once every selected method has executed, and on a method's own state once its
   * routine completes.
   */
  FINISHED,
}
