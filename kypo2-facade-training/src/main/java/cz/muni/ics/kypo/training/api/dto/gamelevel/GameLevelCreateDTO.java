package cz.muni.ics.kypo.training.api.dto.gamelevel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
public class GameLevelCreateDTO {

	@NotNull
	private Long id;
	@NotEmpty(message = "Level title cannot be empty")
	protected String title;
	@NotNull
	@Min(value = 0, message = "Max score cannot be lower than 0")
	@Max(value = 100, message = "Max score cannot be greater than 100")
	private Integer maxScore;
	private String flag;
	private String content;
	private String solution;
	private boolean solutionPenalized;
	private int incorrectFlagLimit;

	public GameLevelCreateDTO() {}

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

	public boolean isSolutionPenalized() {
		return solutionPenalized;
	}

	public void setSolutionPenalized(boolean solutionPenalized) {
		this.solutionPenalized = solutionPenalized;
	}

	public int getIncorrectFlagLimit() {
		return incorrectFlagLimit;
	}

	public void setIncorrectFlagLimit(int incorrectFlagLimit) {
		this.incorrectFlagLimit = incorrectFlagLimit;
	}

	@Override
	public String toString() {
		return "GameLevelCreateDTO{" + "title='" + title + '\'' + ", maxScore=" + maxScore + ", flag='" + flag + '\'' + ", content='" + content
				+ '\'' + ", solution='" + solution + '\'' + ", solutionPenalized=" + solutionPenalized + ", incorrectFlagLimit="
				+ incorrectFlagLimit + '}';
	}
}
