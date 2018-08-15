package cz.muni.ics.kypo.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import cz.muni.ics.kypo.model.enums.AssessmentType;
import cz.muni.ics.kypo.utils.StringJsonUserType;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity(name = "AssessmentLevel")
@Table(name = "assessment_level")
@TypeDefs({@TypeDef(name = "JsonArray", typeClass = StringJsonUserType.class)})
@PrimaryKeyJoinColumn(name = "id")
public class AssessmentLevel extends AbstractLevel implements Serializable {

  @Type(type = "JsonArray")
  @Column(name = "questions", columnDefinition = "jsonb", nullable = false)
  private String questions;
  @Column(name = "instructions", nullable = false)
  private String instructions;
  @Column(name = "assessment_type", length = 128, nullable = false)
  @Enumerated(EnumType.STRING)
  private AssessmentType assessmentType;

  public AssessmentLevel() {}

  public AssessmentLevel(String questions, String instructions, AssessmentType assessmentType) {
    super();
    this.questions = questions;
    this.instructions = instructions;
    this.assessmentType = assessmentType;
  }

  public String getQuestions() {
    return questions;
  }

  public void setQuestions(String questions) {
    this.questions = questions;
  }

  public String getInstructions() {
    return instructions;
  }

  public void setInstructions(String instructions) {
    this.instructions = instructions;
  }

  public AssessmentType getAssessmentType() {
    return assessmentType;
  }

  public void setAssessmentType(AssessmentType assessmentType) {
    this.assessmentType = assessmentType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(questions, instructions, assessmentType);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (!(obj instanceof AssessmentLevel))
      return false;
    AssessmentLevel other = (AssessmentLevel) obj;
    // @formatter:off
    return Objects.equals(assessmentType, other.getAssessmentType()) 
        && Objects.equals(instructions, other.getInstructions())
        && Objects.equals(questions, other.getQuestions());
    // @formatter:on
  }

  @Override
  public String toString() {
    return "AssessmentLevel [questions=" + questions + ", instructions=" + instructions + ", assessmentType=" + assessmentType + ", getId()=" + getId()
        + ", getTitle()=" + getTitle() + ", getMaxScore()=" + getMaxScore() + ", getNextLevel()=" + getNextLevel()
        + ", toString()=" + super.toString() + "]";
  }

}
