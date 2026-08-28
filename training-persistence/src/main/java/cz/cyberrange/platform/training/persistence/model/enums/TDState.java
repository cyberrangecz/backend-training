package cz.cyberrange.platform.training.persistence.model.enums;

/**
 * How far along its life a training definition is, stored by name. A definition may move from
 * unreleased to released, from released to archived, and from released back to unreleased; being
 * put back to unreleased is refused once an instance runs the definition, and any other move is
 * refused outright.
 */
public enum TDState {
  PRIVATED,
  /** Available for instances to run */
  RELEASED,
  /** Retired from use; it may not be moved out of this state */
  ARCHIVED,
  /** Still being authored, visible to its authors and its beta testing group */
  UNRELEASED;
}
