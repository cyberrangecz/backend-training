package cz.muni.ics.kypo.training.persistence.model.question;

import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.enums.QuestionType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "question")
public class Question implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "questionGenerator")
    @SequenceGenerator(name = "questionGenerator", sequenceName = "question_seq")
    @Column(name = "question_id", nullable = false, unique = true)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;
    @Column(name = "text")
    private String text;
    @Column(name = "order_in_assessment")
    private int order;
    @Column(name = "points")
    private int points;
    @Column(name = "penalty")
    private int penalty;
    @Column(name = "answer_required")
    private boolean answerRequired;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_level_id")
    private AssessmentLevel assessmentLevel;
    @OrderBy("order asc")
    @OneToMany(
            mappedBy = "question",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<QuestionChoice> choices = new ArrayList<>();
    @OrderBy("order asc")
    @OneToMany(
            mappedBy = "question",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ExtendedMatchingStatement> extendedMatchingStatements = new ArrayList<>();
    @OrderBy("order asc")
    @OneToMany(
            mappedBy = "question",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ExtendedMatchingOption> extendedMatchingOptions = new ArrayList<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public boolean isAnswerRequired() {
        return answerRequired;
    }

    public void setAnswerRequired(boolean answerRequired) {
        this.answerRequired = answerRequired;
    }

    public AssessmentLevel getAssessmentLevel() {
        return assessmentLevel;
    }

    public void setAssessmentLevel(AssessmentLevel assessmentLevel) {
        this.assessmentLevel = assessmentLevel;
    }

    public List<QuestionChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<QuestionChoice> questionChoices) {
        this.choices = questionChoices;
        this.choices.forEach(choice -> choice.setQuestion(this));
    }

    public List<ExtendedMatchingStatement> getExtendedMatchingStatements() {
        return extendedMatchingStatements;
    }

    public void setExtendedMatchingStatements(List<ExtendedMatchingStatement> extendedMatchingStatements) {
        this.extendedMatchingStatements = extendedMatchingStatements;
        this.extendedMatchingStatements.forEach(statement -> statement.setQuestion(this));
    }

    public List<ExtendedMatchingOption> getExtendedMatchingOptions() {
        return extendedMatchingOptions;
    }

    public void setExtendedMatchingOptions(List<ExtendedMatchingOption> extendedMatchingOptions) {
        this.extendedMatchingOptions = extendedMatchingOptions;
        this.extendedMatchingOptions.forEach(option -> option.setQuestion(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question question = (Question) o;
        return getOrder() == question.getOrder() &&
                getQuestionType() == question.getQuestionType() &&
                Objects.equals(getText(), question.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestionType(), getText(), getOrder());
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + this.getId() +
                ", questionType=" + this.getQuestionType() +
                ", text='" + this.getText() + '\'' +
                ", order=" + this.getOrder() +
                '}';
    }
}
