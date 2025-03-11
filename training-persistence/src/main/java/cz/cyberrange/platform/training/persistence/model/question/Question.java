package cz.cyberrange.platform.training.persistence.model.question;

import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.enums.QuestionType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Question question = (Question) o;
        return getId() != null && Objects.equals(getId(), question.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
