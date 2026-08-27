package cz.cyberrange.platform.training.persistence.model.question;

import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.enums.QuestionType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A row of the {@code question} table, belonging to one {@link AssessmentLevel}. Which of {@link
 * #choices}, {@link #extendedMatchingStatements}, and {@link #extendedMatchingOptions} carries the
 * question's content, and how a submitted answer to it is scored, is decided by {@link
 * #questionType}.
 */
@Getter
@Setter
@Entity
@Table(name = "question")
public class Question implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "questionGenerator")
  @SequenceGenerator(name = "questionGenerator", sequenceName = "question_seq")
  @Column(name = "question_id", nullable = false, unique = true)
  private Long id;

  /**
   * Which shape of answer this question takes, which in turn decides which content collection is
   * populated and which of {@link #choices}' or {@link #extendedMatchingStatements}' correctness
   * data is consulted when scoring a submitted answer.
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "question_type")
  private QuestionType questionType;

  /** The question's prompt, shown to the participant. */
  @Column(name = "text")
  private String text;

  /**
   * Position of the question within its assessment level. Drives the ascending order of the level's
   * {@code questions} list and, together with {@link #questionType} and {@link #text}, this
   * question's equality.
   */
  @Column(name = "order_in_assessment")
  private int order;

  /**
   * Points added to the participant's score for this question when answered correctly in a
   * TEST-type assessment; also summed across a level's questions to compute that level's maximum
   * score. Not applied for any other assessment type, whose answers are recorded without being
   * scored.
   */
  @Column(name = "points")
  private int points;

  /**
   * Points subtracted from the participant's score for this question when answered incorrectly in a
   * TEST-type assessment. Not applied for any other assessment type, whose answers are recorded
   * without being scored.
   */
  @Column(name = "penalty")
  private int penalty;

  /**
   * Whether a response to this question is mandatory. Enforced only for a non-TEST assessment,
   * where submitting a response without one for a required question is rejected; a TEST-type
   * assessment already requires every one of its questions to be answered regardless of this flag.
   */
  @Column(name = "answer_required")
  private boolean answerRequired;

  /**
   * The assessment level this question belongs to, assigned when that level's question list is set.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assessment_level_id")
  private AssessmentLevel assessmentLevel;

  /**
   * For an FFQ question, every text in this list is an accepted answer regardless of its {@code
   * correct} flag. For an MCQ question, this list is the offered choices, and only those with
   * {@code correct} set count toward a correct answer. Unused for an EMI question.
   */
  @OrderBy("order asc")
  @OneToMany(
      mappedBy = "question",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<QuestionChoice> choices = new ArrayList<>();

  /**
   * For an EMI question, the statements to be paired with an option; each carries the option that
   * answers it correctly. Unused for an FFQ or MCQ question.
   */
  @OrderBy("order asc")
  @OneToMany(
      mappedBy = "question",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<ExtendedMatchingStatement> extendedMatchingStatements = new ArrayList<>();

  /**
   * For an EMI question, the options offered to be paired against {@link
   * #extendedMatchingStatements}. Unused for an FFQ or MCQ question.
   */
  @OrderBy("order asc")
  @OneToMany(
      mappedBy = "question",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<ExtendedMatchingOption> extendedMatchingOptions = new ArrayList<>();

  /**
   * Assigns the question's choices, linking each one back to this question.
   *
   * @param questionChoices the choices to assign
   */
  public void setChoices(List<QuestionChoice> questionChoices) {
    this.choices = questionChoices;
    this.choices.forEach(choice -> choice.setQuestion(this));
  }

  public List<ExtendedMatchingStatement> getExtendedMatchingStatements() {
    return extendedMatchingStatements;
  }

  /**
   * Assigns the question's extended matching statements, linking each one back to this question.
   *
   * @param extendedMatchingStatements the statements to assign
   */
  public void setExtendedMatchingStatements(
      List<ExtendedMatchingStatement> extendedMatchingStatements) {
    this.extendedMatchingStatements = extendedMatchingStatements;
    this.extendedMatchingStatements.forEach(statement -> statement.setQuestion(this));
  }

  public List<ExtendedMatchingOption> getExtendedMatchingOptions() {
    return extendedMatchingOptions;
  }

  /**
   * Assigns the question's extended matching options, linking each one back to this question.
   *
   * @param extendedMatchingOptions the options to assign
   */
  public void setExtendedMatchingOptions(List<ExtendedMatchingOption> extendedMatchingOptions) {
    this.extendedMatchingOptions = extendedMatchingOptions;
    this.extendedMatchingOptions.forEach(option -> option.setQuestion(this));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Question)) return false;
    Question question = (Question) o;
    return getOrder() == question.getOrder()
        && getQuestionType() == question.getQuestionType()
        && Objects.equals(getText(), question.getText());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getQuestionType(), getText(), getOrder());
  }

  @Override
  public String toString() {
    return "Question{"
        + "id="
        + this.getId()
        + ", questionType="
        + this.getQuestionType()
        + ", text='"
        + this.getText()
        + '\''
        + ", order="
        + this.getOrder()
        + '}';
  }
}
