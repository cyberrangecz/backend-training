package cz.cyberrange.platform.training.persistence.model.enums;

/**
 * How far along its life one trainee's attempt at a training instance is, stored by name. A run is
 * running from the moment it is created; it can be resumed only while it is neither finished nor
 * archived.
 */
public enum TRState {
  RUNNING,
  /** The trainee has worked through the last level. */
  FINISHED,
  /** Kept for the record with its sandbox given up. */
  ARCHIVED;
}
