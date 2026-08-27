package cz.cyberrange.platform.training.api.enums;

/** Classifies how an assessment question is answered and how its answers are handled */
public enum QuestionType {
  /** A free-text question; the training run preview clears any predefined choices for it */
  FFQ,
  MCQ,
  /**
   * A question with extended matching statements; a submitted answer is resolved to a statement and
   * option order rather than compared as free text, and its correct option is validated when saved
   * under a test-graded assessment
   */
  EMI
}
