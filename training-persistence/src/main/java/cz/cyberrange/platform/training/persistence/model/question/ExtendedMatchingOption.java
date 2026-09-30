package cz.cyberrange.platform.training.persistence.model.question;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A row of the {@code extend_matching_option} table: one option offered in an EMI {@link Question},
 * matched against by an {@link ExtendedMatchingStatement}
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "extend_matching_option")
public class ExtendedMatchingOption {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "extendedMatchingOptionGenerator")
  @SequenceGenerator(
      name = "extendedMatchingOptionGenerator",
      sequenceName = "extended_matching_option_seq")
  @Column(name = "extend_matching_option_id", nullable = false, unique = true)
  private Long id;

  /** The option's text, shown to the participant */
  @Column(name = "text")
  private String text;

  /**
   * Position of the option among its question's options, expected to match the option's index in
   * {@link Question#getExtendedMatchingOptions()}. Reported as the identifier of the option a
   * participant pairs with a statement, and of the option an {@link ExtendedMatchingStatement}
   * names as its correct pairing.
   */
  @Column(name = "order_in_row")
  private int order;

  /** The question this option belongs to */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private Question question;
}
