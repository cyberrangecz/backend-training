package cz.muni.ics.kypo.training.api.dto.imports;

import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelImportDTO}
 *
 */
@ApiModel(value = "AssessmentLevelImportDTO", description = "Imported assessment level.", parent = AbstractLevelImportDTO.class)
public class AssessmentLevelImportDTO extends AbstractLevelImportDTO {

	@ApiModelProperty(value = "Questions of assessment level to update.")
	@NotNull(message = "{assessmentLevel.questions.NotNull.message}")
	private List<QuestionDTO> questions = new ArrayList<>();
	@ApiModelProperty(value = "Instructions of assessment level to update.", example = "Fill me up slowly")
	@NotNull(message = "{assessmentLevel.instructions.NotNull.message}")
	private String instructions;
	@ApiModelProperty(value = "Type of assessment level to update.", required = true, example = "TEST")
	@NotNull(message = "{assessmentLevel.type.NotNull.message}")
	private AssessmentType assessmentType;

	/**
	 * Gets questions.
	 *
	 * @return the questions
	 */
	public List<QuestionDTO> getQuestions() {
		return questions;
	}

	/**
	 * Sets questions.
	 *
	 * @param questions the questions
	 */
	public void setQuestions(List<QuestionDTO> questions) {
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
