package cz.muni.ics.kypo.training.api.dto.gamelevel;

import java.util.Arrays;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiModel(value = "GameLevelUpdateDTO", description = "Game Level to update.")
public class GameLevelUpdateDTO {

	protected Long id;
	@NotEmpty(message = "Level title cannot be empty")
	protected String title;
	@NotNull
	@Min(value = 0, message = "Max score cannot be lower than 0")
	@Max(value = 100, message = "Max score cannot be greater than 100")
	private Integer maxScore;
	private Long nextLevel;
	private String flag;
	private String content;
	private String solution;
	private int incorrectFlagPenalty;
	private int solutionPenalty = maxScore - 1;
	private int estimatedDuration;
	private String[] attachments;
	private int incorrectFlagLimit;

	public GameLevelUpdateDTO() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

	public int getIncorrectFlagPenalty() {
		return incorrectFlagPenalty;
	}

	public void setIncorrectFlagPenalty(int incorrectFlagPenalty) {
		this.incorrectFlagPenalty = incorrectFlagPenalty;
	}

	public int getSolutionPenalty() {
		return solutionPenalty;
	}

	public void setSolutionPenalty(int solutionPenalty) {
		this.solutionPenalty = solutionPenalty;
	}

	public int getEstimatedDuration() {
		return estimatedDuration;
	}

	public void setEstimatedDuration(int estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	public String[] getAttachments() {
		return attachments;
	}

	public void setAttachments(String[] attachments) {
		this.attachments = attachments;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}

	public Long getNextLevel() {
		return nextLevel;
	}

	public void setNextLevel(Long nextLevel) {
		this.nextLevel = nextLevel;
	}

	public int getIncorrectFlagLimit() {
		return incorrectFlagLimit;
	}

	public void setIncorrectFlagLimit(int incorrectFlagLimit) {
		this.incorrectFlagLimit = incorrectFlagLimit;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GameLevelUpdateDTO [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", maxScore=");
		builder.append(maxScore);
		builder.append(", nextLevel=");
		builder.append(nextLevel);
		builder.append(", flag=");
		builder.append(flag);
		builder.append(", content=");
		builder.append(content);
		builder.append(", solution=");
		builder.append(solution);
		builder.append(", incorrectFlagPenalty=");
		builder.append(incorrectFlagPenalty);
		builder.append(", solutionPenalty=");
		builder.append(solutionPenalty);
		builder.append(", estimatedDuration=");
		builder.append(estimatedDuration);
		builder.append(", attachments=");
		builder.append(Arrays.toString(attachments));
		builder.append(", incorrectFlagLimit=");
		builder.append(incorrectFlagLimit);
		builder.append("]");
		return builder.toString();
	}

}
