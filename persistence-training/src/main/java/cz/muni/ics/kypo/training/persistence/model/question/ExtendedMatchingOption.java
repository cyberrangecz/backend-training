package cz.muni.ics.kypo.training.persistence.model.question;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "extend_matching_option")
public class ExtendedMatchingOption {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "extendedMatchingOptionGenerator")
    @SequenceGenerator(name = "extendedMatchingOptionGenerator", sequenceName = "extend_matching_option_seq")
    @Column(name = "extend_matching_option_id", nullable = false, unique = true)
    private Long id;
    @Column(name = "text")
    private String text;
    @Column(name = "order_in_row")
    private int order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

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

    @Override
    public String toString() {
        return "EMIOption{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", order=" + order +
                '}';
    }
}
