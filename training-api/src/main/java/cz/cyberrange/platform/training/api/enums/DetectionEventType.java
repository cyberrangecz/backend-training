package cz.cyberrange.platform.training.api.enums;

/** Classifies which detection method produced a recorded cheating detection event */
public enum DetectionEventType {

  /** Tags an event raised when two participants' submissions resolve to the same IP address */
  LOCATION_SIMILARITY,
  /** Tags an event raised when a submitted answer matches one generated for another participant */
  ANSWER_SIMILARITY,
  /** Tags an event raised when a level is solved faster than its configured minimal time */
  MINIMAL_SOLVE_TIME,
  /** Tags an event raised when participants submit a level within a configured time threshold */
  TIME_PROXIMITY,
  /** Tags an event raised when no console command was recorded before a level's submission */
  NO_COMMANDS,
  /** Tags an event raised when a recorded console command matches a forbidden command */
  FORBIDDEN_COMMANDS;
}
