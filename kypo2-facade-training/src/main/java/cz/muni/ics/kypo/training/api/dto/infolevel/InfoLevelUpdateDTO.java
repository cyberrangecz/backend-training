package cz.muni.ics.kypo.training.api.dto.infolevel;

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
@ApiModel(value = "InfoLevelUpdateDTO", description = "Info Level to update.")
public class InfoLevelUpdateDTO {

	protected Long id;
	@NotEmpty(message = "Level title cannot be empty")
	protected String title;
	@NotNull
	@Min(value = 0, message = "Max score cannot be lower than 0")
	@Max(value = 100, message = "Max score cannot be greater than 100")
	protected Integer maxScore;
	protected Long nextLevel;
	private String content;

	public InfoLevelUpdateDTO() {}

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InfoLevelUpdateDTO [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", maxScore=");
		builder.append(maxScore);
		builder.append(", nextLevel=");
		builder.append(nextLevel);
		builder.append(", content=");
		builder.append(content);
		builder.append("]");
		return builder.toString();
	}

}
