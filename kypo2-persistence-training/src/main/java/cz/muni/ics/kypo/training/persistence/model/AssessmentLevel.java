package cz.muni.ics.kypo.training.persistence.model;


import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.question.Question;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Class specifying Abstract level as assessment level.
 * Assessment levels contain questions for trainees to answer.
 */
@Entity
@Table(name = "assessment_level")
@PrimaryKeyJoinColumn(name = "id")
public class AssessmentLevel extends AbstractLevel {

    @OrderBy("order asc")
    @OneToMany(
            mappedBy = "assessmentLevel",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Question> questions = new ArrayList<>();
    @Column(name = "instructions", nullable = false)
    private String instructions;
    @Column(name = "assessment_type", length = 128, nullable = false)
    @Enumerated(EnumType.STRING)
    private AssessmentType assessmentType;

    /**
     * Instantiates a new Assessment level
     */
    public AssessmentLevel() {
    }

    /**
     * Instantiates a new Assessment level
     *
     * @param questions      questions that trainee must answer
     * @param instructions   instructions to help trainee understand the questions
     * @param assessmentType type of assessment level. Types are QUESTIONNAIRE and TEST
     */
    public AssessmentLevel(List<Question> questions, String instructions, AssessmentType assessmentType) {
        super();
        this.questions = questions;
        this.instructions = instructions;
        this.assessmentType = assessmentType;
    }

    /**
     * Gets questions that trainee must answer
     *
     * @return the questions
     */
    public List<Question> getQuestions() {
        return questions;
    }

    /**
     * Sets questions that trainee must answer
     *
     * @param questions the questions
     */
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        this.questions.forEach(question -> question.setAssessmentLevel(this));
        this.questions.sort(Comparator.comparingInt(Question::getOrder));
    }

    /**
     * Gets instructions to help trainee understand the questions
     *
     * @return the instructions
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Sets instructions to help trainee understand the questions
     *
     * @param instructions the instructions
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * Gets type of assessment level. Types are QUESTIONNAIRE and TEST
     *
     * @return the assessment type
     */
    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    /**
     * Sets type of assessment level. Types are QUESTIONNAIRE and TEST
     *
     * @param assessmentType the assessment type
     */
    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssessmentLevel)) return false;
        if (!super.equals(o)) return false;
        AssessmentLevel that = (AssessmentLevel) o;
        return Objects.equals(getQuestions(), that.getQuestions()) &&
                Objects.equals(getInstructions(), that.getInstructions()) &&
                getAssessmentType() == that.getAssessmentType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getQuestions(), getInstructions(), getAssessmentType());
    }

    @Override
    public String toString() {
        return "AssessmentLevel{" +
                "questions='" + questions + '\'' +
                ", instructions='" + instructions + '\'' +
                ", assessmentType=" + assessmentType +
                '}';
    }
}
