package cz.cyberrange.platform.training.persistence.model.question;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A row of the {@code extended_matching_statement} table: one statement of an EMI {@link Question},
 * to be paired by the participant with one of the question's {@link ExtendedMatchingOption}s
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "extended_matching_statement")
public class ExtendedMatchingStatement {

  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "extendedMatchingStatementGenerator")
  @SequenceGenerator(
      name = "extendedMatchingStatementGenerator",
      sequenceName = "extended_matching_statement_seq")
  @Column(name = "extended_matching_statement_id", nullable = false, unique = true)
  private Long id;

  /** The statement's text, shown to the participant */
  @Column(name = "text")
  private String text;

  /**
   * Position of the statement among its question's statements, expected to match the statement's
   * index in {@link Question#getExtendedMatchingStatements()}. Reported as the identifier of the
   * statement a participant's paired answer refers to.
   */
  @Column(name = "order_in_column")
  private int order;

  /** The question this statement belongs to */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private Question question;

  /**
   * The option that correctly answers this statement. Resolved by indexing the question's extended
   * matching options at this statement's designated correct order, when the question is created,
   * imported, or updated. Read to decide whether a submitted pairing is correct and to report the
   * expected pairing.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "extended_matching_option_id")
  private ExtendedMatchingOption extendedMatchingOption;
}
