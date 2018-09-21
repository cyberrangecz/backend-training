package cz.muni.ics.kypo.training.api.dto.gamelevel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
public class GameLevelCreateDTO {

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
	private boolean solutionPenalized;
	private int estimatedDuration;
	private String[] attachments;
	private int incorrectFlagLimit;

	public GameLevelCreateDTO() {}

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

	public int getIncorrectFlagLimit() {
		return incorrectFlagLimit;
	}

	public void setIncorrectFlagLimit(int incorrectFlagLimit) {
		this.incorrectFlagLimit = incorrectFlagLimit;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GameLevelCreateDTO [title=");
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
		builder.append(", solutionPenalized=");
		builder.append(solutionPenalized);
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
