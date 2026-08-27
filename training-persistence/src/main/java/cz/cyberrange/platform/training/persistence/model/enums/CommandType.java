package cz.cyberrange.platform.training.persistence.model.enums;

/**
 * Which console a command belongs to. A recorded command counts as forbidden only when its own
 * console matches, so the kind narrows the match as much as the command text does.
 */
public enum CommandType {

  /** The shell console */
  BASH,
  /** The Metasploit console */
  MSF;
}
