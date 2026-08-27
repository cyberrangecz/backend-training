package cz.cyberrange.platform.training.api.enums;

/**
 * Classifies whether an assessment level's submitted answers are graded against correct answers or
 * only collected.
 */
public enum AssessmentType {

  /**
   * Requires the correct option of every extended matching statement to be resolved when the level
   * is saved, has submitted answers evaluated against those answers, and counts toward the training
   * run's score.
   */
  TEST,
  /**
   * The default type assigned to a newly created assessment level. Submitted answers are collected
   * without evaluation, and the level does not count toward the training run's score.
   */
  QUESTIONNAIRE;
}
