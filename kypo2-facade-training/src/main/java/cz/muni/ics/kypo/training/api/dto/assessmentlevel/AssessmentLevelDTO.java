package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 *
 * @author Dominik Pilár (445537)
 *
 */
@ApiModel(value = "AssessmentLevelDTO", description = "Information about assessment level.")
public class AssessmentLevelDTO extends AbstractLevelDTO {

	@ApiModelProperty(value = "Assessment level questions.")
	private String questions;
	@ApiModelProperty(value = "Assessment level instructions.")
	private String instructions;
	@ApiModelProperty(value = "Assessment level assessment type.")
	private AssessmentType assessmentType;

	public AssessmentLevelDTO() {}

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
		return "AssessmentLevelDTO [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", nextLevel=" + nextLevel + ", preHook="
				+ preHook + ", postHook=" + postHook + ", questions=" + questions + ", instructions=" + instructions + ", type="
				+ assessmentType.name() + "]";
	}

}
