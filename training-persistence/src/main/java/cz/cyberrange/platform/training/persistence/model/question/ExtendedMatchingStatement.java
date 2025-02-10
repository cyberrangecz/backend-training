package cz.cyberrange.platform.training.persistence.model.question;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "extended_matching_statement")
public class ExtendedMatchingStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "extendedMatchingStatementGenerator")
    @SequenceGenerator(name = "extendedMatchingStatementGenerator")
    @Column(name = "extended_matching_statement_id", nullable = false, unique = true)
    private Long id;
    @Column(name = "text")
    private String text;
    @Column(name = "order_in_column")
    private int order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extended_matching_option_id")
    private ExtendedMatchingOption extendedMatchingOption;
}
