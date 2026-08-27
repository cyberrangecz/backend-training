package cz.cyberrange.platform.training.api.enums;

/**
 * Classifies the console a forbidden command definition matches against a training run's recorded
 * console commands.
 */
public enum CommandType {

  /** Matches a recorded command whose console type is the bash console. */
  BASH,
  /** Matches a recorded command whose console type is msfconsole. */
  MSF;
}
