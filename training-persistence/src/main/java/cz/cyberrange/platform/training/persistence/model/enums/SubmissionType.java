package cz.cyberrange.platform.training.persistence.model.enums;

/**
 * Whether a recorded answer submission was accepted as the expected one. Stored by name on every
 * submission, and the distinction the cheating detections read submissions by.
 */
public enum SubmissionType {
  CORRECT,
  INCORRECT;
}
