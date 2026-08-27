package cz.cyberrange.platform.training.api.dto.archive;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Data;

/**
 * Tally of every answer submitted for one question across every training run of a training
 * instance's assessment, written as one JSON entry into the training instance archive.
 */
@Data
public class QuestionAnswersDetailsDTO {
  /** Text of the question this tally covers. */
  private String question;

  /**
   * Count of how many times each distinct submitted answer value appeared, keyed by that value.
   * Accumulates across every {@link #addAnswers} call.
   */
  private Map<String, Integer> answers = new HashMap<>();

  /**
   * Number of {@link #addAnswers} calls received, one per training run that answered this question.
   * A trainee holding several runs of the instance counts once per run.
   */
  private int totalAnswers;

  /**
   * Creates a tally for the given question text, with no answers recorded yet.
   *
   * @param question text of the question this tally covers
   */
  public QuestionAnswersDetailsDTO(String question) {
    this.question = question;
  }

  /**
   * Folds one participant's submitted answer values into the running tally: increments {@link
   * #totalAnswers} once, and for each value in the set increments its count in {@link #answers},
   * starting a new entry at one if the value has not been seen before.
   *
   * @param answers the answer values submitted by one participant for this question
   */
  public void addAnswers(Set<String> answers) {
    totalAnswers++;
    for (String answer : answers) {
      Integer numberOfAnswers = this.answers.getOrDefault(answer, 0) + 1;
      this.answers.put(answer, numberOfAnswers);
    }
  }
}
