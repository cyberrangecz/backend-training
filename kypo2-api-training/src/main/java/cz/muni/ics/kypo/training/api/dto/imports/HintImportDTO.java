package cz.muni.ics.kypo.training.api.dto.imports;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 *  Encapsulates information about Hint.
 *
 */
@ApiModel(value = "HintImportDTO", description = "Imported hint.")
public class HintImportDTO {

	@ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
	private String title;
	@ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Very good advice")
	private String content;
	@ApiModelProperty(value = "The number of points the participant loses after receiving the hint.", example = "10")
	private Integer hintPenalty;

	/**
	 * Gets title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets title.
	 *
	 * @param title the title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets content.
	 *
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Sets content.
	 *
	 * @param content the content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Gets hint penalty.
	 *
	 * @return the hint penalty
	 */
	public Integer getHintPenalty() {
		return hintPenalty;
	}

	/**
	 * Sets hint penalty.
	 *
	 * @param hintPenalty the hint penalty
	 */
	public void setHintPenalty(Integer hintPenalty) {
		this.hintPenalty = hintPenalty;
	}

	@Override
	public String toString() {
		return "HintImportDTO{" +
				"title='" + title + '\'' +
				", content='" + content + '\'' +
				", hintPenalty=" + hintPenalty +
				'}';
	}
}
