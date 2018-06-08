package cz.muni.ics.kypo.model;

import java.util.Set;

import javax.persistence.Column;

import org.hibernate.annotations.Type;

import cz.muni.ics.kypo.model.enums.AssessmentType;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public class AssessmentLevel extends AbstractLevel {

  @Type(type = "jsonb")
  @Column(name = "questions", columnDefinition = "json", nullable = false)
  private String questions;
  @Column(name = "instructions", nullable = false)
  private String instructions;
  @Column(name = "assessment_type", nullable = false)
  private AssessmentType assessmentType;

  public AssessmentLevel() {}

  public AssessmentLevel(String questions, String instructions, AssessmentType assessmentType) {
    super();
    this.questions = questions;
    this.instructions = instructions;
    this.assessmentType = assessmentType;
  }

  public AssessmentLevel(Long id, String title, int maxScore, int order, byte[] preHook, byte[] postHook, Long nextLevel, TrainingDefinition trainingDefinition,
      Set<TrainingRun> trainingRun) {
    super(id, title, maxScore, order, preHook, postHook, nextLevel, trainingDefinition, trainingRun);
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
  public String toString() {
    return "AssessmentLevel [questions=" + questions + ", instructions=" + instructions + ", assessmentType=" + assessmentType + "]";
  }

}
