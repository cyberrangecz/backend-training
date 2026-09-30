package cz.cyberrange.platform.training.persistence.model.enums;

/**
 * What an assessment level is for, which decides whether the answers given to it are marked. Stored
 * by name.
 */
public enum AssessmentType {

  /**
   * Answers are marked against the correct ones and the level awards score, its maximum being the
   * sum of its questions' points
   */
  TEST,
  /** Answers are collected without being marked, and the level awards no score */
  QUESTIONNAIRE;
}
