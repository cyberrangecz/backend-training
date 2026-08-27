package cz.cyberrange.platform.training.persistence.model.enums;

/**
 * What shape of answer a question of an assessment level takes, which decides which of the
 * question's collections carry its content and how a submitted answer is marked. Stored by name.
 */
public enum QuestionType {
  /** Answered by typing text, which is matched against the accepted answers. */
  FFQ,
  /** Answered by pairing each statement with one of the offered options. */
  EMI,
  /** Answered by picking from the offered choices. */
  MCQ;
}
