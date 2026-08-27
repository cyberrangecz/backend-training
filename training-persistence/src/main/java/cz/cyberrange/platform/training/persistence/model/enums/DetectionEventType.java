package cz.cyberrange.platform.training.persistence.model.enums;

/**
 * What a cheating detection finding accuses a trainee of, recorded on the finding and stored by
 * name. Each kind is produced by its own detection.
 */
public enum DetectionEventType {

  /** cheat type representing two players having the same IP address */
  LOCATION_SIMILARITY,
  /**
   * cheat type representing a player submitting an answer that was generated for another player
   */
  ANSWER_SIMILARITY,
  /** cheat type representing a player solving a task in less than minimal possible solve time */
  MINIMAL_SOLVE_TIME,
  /** cheat type representing two players submitting their levels at similar times */
  TIME_PROXIMITY,
  /**
   * A trainee solving a level without any console command being recorded for it. Only a level
   * marked as requiring commands is examined, and a level whose solution was revealed is passed
   * over.
   */
  NO_COMMANDS,
  /** cheat type representing a player using commands that are forbidden */
  FORBIDDEN_COMMANDS;
}
