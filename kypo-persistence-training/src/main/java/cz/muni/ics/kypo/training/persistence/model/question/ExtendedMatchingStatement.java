package cz.muni.ics.kypo.training.persistence.model.question;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "extended_matching_statement")
public class ExtendedMatchingStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "extendedMatchingStatementGenerator")
    @SequenceGenerator(name = "extendedMatchingStatementGenerator", sequenceName = "extended_matching_statement_seq")
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public ExtendedMatchingOption getExtendedMatchingOption() {
        return extendedMatchingOption;
    }

    public void setExtendedMatchingOption(ExtendedMatchingOption extendedMatchingOption) {
        this.extendedMatchingOption = extendedMatchingOption;
    }


    @Override
    public String toString() {
        return "ExtendedMatchingStatement{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", order=" + order +
                '}';
    }
}
