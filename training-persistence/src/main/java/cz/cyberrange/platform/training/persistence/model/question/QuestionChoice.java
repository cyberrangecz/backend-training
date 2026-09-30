package cz.cyberrange.platform.training.persistence.model.question;

import java.io.Serializable;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A row of the {@code question_choice} table: one answer option of an FFQ or MCQ {@link Question}
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "question_choice")
public class QuestionChoice implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "questionChoiceGenerator")
  @SequenceGenerator(name = "questionChoiceGenerator", sequenceName = "question_choice_seq")
  @Column(name = "question_choice_id", nullable = false, unique = true)
  private Long id;

  /**
   * The choice's text. For an FFQ question, every choice's text is an accepted answer regardless of
   * {@link #correct}; for an MCQ question, it is compared against the text of every choice the
   * participant selects.
   */
  @Column(name = "text")
  private String text;

  /**
   * Whether this choice counts toward a correct answer. Consulted only for an MCQ question, where a
   * submitted answer is correct when it selects the text of exactly the choices with this flag set;
   * ignored for an FFQ question.
   */
  @Column(name = "correct")
  private boolean correct;

  /**
   * Position of the choice among its question's choices. Drives the ascending order of {@link
   * Question#getChoices()} and is reported as the identifier of a choice the participant selects in
   * an MCQ question.
   */
  @Column(name = "order_in_question")
  private int order;

  /** The question this choice belongs to */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private Question question;
}
