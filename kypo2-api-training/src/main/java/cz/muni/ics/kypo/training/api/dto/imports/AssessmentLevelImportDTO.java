package cz.muni.ics.kypo.training.api.dto.imports;

import cz.muni.ics.kypo.training.api.enums.AssessmentType;

/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelImportDTO}
 *
 * @author Boris Jadus(445343)
 */
public class AssessmentLevelImportDTO extends AbstractLevelImportDTO {

	private String questions;
	private String instructions;
	private AssessmentType assessmentType;

	/**
	 * Gets questions.
	 *
	 * @return the questions
	 */
	public String getQuestions() {
		return questions;
	}

	/**
	 * Sets questions.
	 *
	 * @param questions the questions
	 */
	public void setQuestions(String questions) {
		this.questions = questions;
	}

	/**
	 * Gets instructions.
	 *
	 * @return the instructions
	 */
	public String getInstructions() {
		return instructions;
	}

	/**
	 * Sets instructions.
	 *
	 * @param instructions the instructions
	 */
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * Gets assessment type.
	 *
	 * @return the {@link AssessmentType}
	 */
	public AssessmentType getAssessmentType() {
		return assessmentType;
	}

	/**
	 * Sets assessment type.
	 *
	 * @param assessmentType the {@link AssessmentType}
	 */
	public void setAssessmentType(AssessmentType assessmentType) {
		this.assessmentType = assessmentType;
	}

	@Override public String toString() {
		return "AssessmentLevelImportDTO{" + "questions='" + questions + '\'' + ", instructions='" + instructions + '\'' + ", assessmentType="
				+ assessmentType + '}';
	}
}
