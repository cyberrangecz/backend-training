package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import cz.muni.ics.kypo.training.model.enums.AssessmentType;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 
 * @author SedaQ
 *
 */
/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiModel(value = "AssessmentLevelCreateDTO", description = "Assessment Level to create.")
public class AssessmentLevelCreateDTO {

	@NotNull
	private Long id;
	@NotEmpty(message = "{assessmentlevelcreate.title.NotEmpty.message}")
	private String title;
	@NotNull(message = "{assessmentlevelcreate.maxScore.NotNull.message}")
	@Min(value = 0, message = "{assessmentlevelcreate.maxScore.Min.message}")
	@Max(value = 100, message = "{assessmentlevelcreate.maxScore.Max.message}")
	private Integer maxScore;
	private String questions;
	private String instructions;
	private AssessmentType type;

	public AssessmentLevelCreateDTO() {}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Integer maxScore) {
		this.maxScore = maxScore;
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

	public AssessmentType getType() {
		return type;
	}

	public void setType(AssessmentType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AssessmentLevelCreateDTO [title=");
		builder.append(title);
		builder.append(", maxScore=");
		builder.append(maxScore);
		builder.append(", questions=");
		builder.append(questions);
		builder.append(", instructions=");
		builder.append(instructions);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}

}
