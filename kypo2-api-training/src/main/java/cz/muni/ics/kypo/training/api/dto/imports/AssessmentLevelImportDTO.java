package cz.muni.ics.kypo.training.api.dto.imports;

import cz.muni.ics.kypo.training.api.enums.AssessmentType;

/**
 * @author Boris Jadus(445343)
 */

public class AssessmentLevelImportDTO extends AbstractLevelImportDTO {

	private String questions;
	private String instructions;
	private AssessmentType assessmentType;

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

	@Override public String toString() {
		return "AssessmentLevelImportDTO{" + "questions='" + questions + '\'' + ", instructions='" + instructions + '\'' + ", assessmentType="
				+ assessmentType + '}';
	}
}
