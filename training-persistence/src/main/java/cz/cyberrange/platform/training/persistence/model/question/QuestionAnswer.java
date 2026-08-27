package cz.cyberrange.platform.training.persistence.model.question;

import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.ToString;

/**
 * A row of the {@code question_answer} table: one participant's submitted response to one {@link
 * Question} within one {@link TrainingRun}, keyed by the pair of their primary keys.
 *
 * <p>{@code QuestionAnswer.getAllByQuestionIdAndInstanceId} finds every answer for a given question
 * and training instance, with its {@link Question}, {@link TrainingRun}, and that run's {@code
 * TrainingInstance} loaded along with it.
 */
@NamedQueries({
  @NamedQuery(
      name = "QuestionAnswer.getAllByQuestionIdAndInstanceId",
      query =
          "SELECT qa FROM QuestionAnswer qa "
              + "JOIN FETCH qa.question q "
              + "JOIN FETCH qa.trainingRun tr "
              + "JOIN FETCH tr.trainingInstance ti "
              + "WHERE q.id = :questionId AND ti.id = :instanceId")
})
@ToString
@Entity
@Table(name = "question_answer")
public class QuestionAnswer implements Serializable {

  @EmbeddedId private QuestionAnswerId questionAnswerId;

  /** The question this answer responds to */
  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("questionId")
  @JoinColumn(name = "question_id")
  private Question question;

  /** The training run in which this answer was submitted */
  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("trainingRunId")
  @JoinColumn(name = "training_run_id")
  private TrainingRun trainingRun;

  /**
   * The submitted answer, in the form decided by {@link #question}'s {@code questionType}: for an
   * FFQ question, the typed text; for an MCQ question, the text of every choice selected; for an
   * EMI question, one {@code { "statementOrder": ..., "optionOrder": ... }} string per statement
   * the participant paired with an option
   */
  @ElementCollection
  @CollectionTable(
      name = "question_answers",
      joinColumns = {@JoinColumn(name = "question_id"), @JoinColumn(name = "training_run_id")})
  @Column(name = "answer")
  private Set<String> answers = new HashSet<>();

  public QuestionAnswer() {
    this.questionAnswerId = new QuestionAnswerId();
  }

  /**
   * Builds an answer for the given question and training run, deriving the composite id from their
   * primary keys.
   *
   * @param question the question being answered
   * @param trainingRun the training run in which it is being answered
   */
  public QuestionAnswer(Question question, TrainingRun trainingRun) {
    this.question = question;
    this.trainingRun = trainingRun;
    this.questionAnswerId = new QuestionAnswerId(question.getId(), trainingRun.getId());
  }

  public QuestionAnswerId getQuestionAnswerId() {
    return questionAnswerId;
  }

  public void setQuestionAnswerId(QuestionAnswerId questionAnswerId) {
    this.questionAnswerId = questionAnswerId;
  }

  public Question getQuestion() {
    return question;
  }

  /**
   * Assigns the answered question, keeping the composite id's question component in step with it.
   *
   * @param question the question being answered
   */
  public void setQuestion(Question question) {
    this.questionAnswerId.setQuestionId(question.getId());
    this.question = question;
  }

  public TrainingRun getTrainingRun() {
    return trainingRun;
  }

  /**
   * Assigns the training run this answer belongs to, keeping the composite id's training run
   * component in step with it.
   *
   * @param trainingRun the training run in which the answer is submitted
   */
  public void setTrainingRun(TrainingRun trainingRun) {
    this.questionAnswerId.setTrainingRunId(trainingRun.getId());
    this.trainingRun = trainingRun;
  }

  public Set<String> getAnswers() {
    return answers;
  }

  public void setAnswers(Set<String> answers) {
    this.answers = answers;
  }
}
