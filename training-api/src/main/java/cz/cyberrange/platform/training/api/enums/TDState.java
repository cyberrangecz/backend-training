package cz.cyberrange.platform.training.api.enums;

/**
 * Classifies the lifecycle state of a training definition. The allowed transitions are unreleased
 * to released, released to archived, and released back to unreleased (refused while the definition
 * has a training instance); requesting the current state is a no-op, and any other transition is
 * refused.
 */
public enum TDState {
  PRIVATED,
  RELEASED,
  ARCHIVED,
  UNRELEASED;
}
