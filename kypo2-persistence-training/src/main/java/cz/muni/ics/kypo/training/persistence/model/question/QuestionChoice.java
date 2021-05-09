package cz.muni.ics.kypo.training.persistence.model.question;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "question_choice")
public class QuestionChoice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "questionChoiceGenerator")
    @SequenceGenerator(name = "questionChoiceGenerator", sequenceName = "question_choice_seq")
    @Column(name = "question_choice_id", nullable = false, unique = true)
    private Long id;
    @Column(name = "text")
    private String text;
    @Column(name = "correct")
    private boolean correct;
    @Column(name = "order_in_question")
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

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
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
        return "QuestionChoice{" +
                "id=" + this.getId() +
                ", text='" + this.getText() + '\'' +
                ", correct=" + this.isCorrect() +
                ", order=" + this.getOrder() +
                '}';
    }
}
