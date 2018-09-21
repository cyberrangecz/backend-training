package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import cz.muni.ics.kypo.training.model.enums.AssessmentType;
import io.swagger.annotations.ApiModel;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiModel(value = "AssessmentLevelUpdateDTO", description = "Assessment Level to update.")
public class AssessmentLevelUpdateDTO {

	@NotNull(message = "{assessmentlevelupdate.id.NotNull.message}")
	protected Long id;
	@NotEmpty(message = "{assessmentlevelupdate.title.NotEmpty.message}")
	private String title;
	@NotNull(message = "{assessmentlevelupdate.maxScore.NotNull.message}")
	@Min(value = 0, message = "{assessmentlevelupdate.maxScore.Min.message}")
	@Max(value = 100, message = "{assessmentlevelupdate.maxScore.Max.message}")
	private Integer maxScore;
	protected Long nextLevel;
	private String questions;
	private String instructions;
	private AssessmentType type;

	public AssessmentLevelUpdateDTO() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Long getNextLevel() {
		return nextLevel;
	}

	public void setNextLevel(Long nextLevel) {
		this.nextLevel = nextLevel;
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
		builder.append("AssessmentLevelUpdateDTO [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", maxScore=");
		builder.append(maxScore);
		builder.append(", nextLevel=");
		builder.append(nextLevel);
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
