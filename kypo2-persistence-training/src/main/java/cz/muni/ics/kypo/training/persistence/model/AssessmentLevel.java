package cz.muni.ics.kypo.training.persistence.model;

import org.hibernate.annotations.Type;

import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;

import javax.persistence.*;
import java.util.Objects;

/**
 * Class specifying Abstract level as assessment level.
 * Assessment levels contain questions for trainees to answer.
 */
@Entity
@Table(name = "assessment_level")
@PrimaryKeyJoinColumn(name = "id")
public class AssessmentLevel extends AbstractLevel {

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "questions", nullable = false)
    private String questions;
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
    public AssessmentLevel(String questions, String instructions, AssessmentType assessmentType) {
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
    public String getQuestions() {
        return questions;
    }

    /**
     * Sets questions that trainee must answer
     *
     * @param questions the questions
     */
    public void setQuestions(String questions) {
        this.questions = questions;
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
