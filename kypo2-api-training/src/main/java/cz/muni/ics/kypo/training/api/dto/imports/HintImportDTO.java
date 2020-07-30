package cz.muni.ics.kypo.training.api.dto.imports;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Encapsulates information about Hint.
 */
@ApiModel(value = "HintImportDTO", description = "Imported hint.")
public class HintImportDTO {

	@ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
	@NotEmpty(message = "{hintimport.title.NotEmpty.message}")
	private String title;
	@ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Very good advice")
	@NotEmpty(message = "{hintimport.content.NotEmpty.message}")
	private String content;
	@ApiModelProperty(value = "The number of points the participant loses after receiving the hint.", example = "10")
	@NotNull(message = "{hintimport.hintPenalty.NotNull.message}")
	@Min(value = 0, message = "{hintimport.hintPenalty.Min.message}")
	@Max(value = 100, message = "{hintimport.hintPenalty.Max.message}")
	private Integer hintPenalty;
	@NotNull(message = "{hintimport.order.NotNull.message}")
	@Min(value = 0, message = "{hintimport.order.Min.message}")
	@ApiModelProperty(value = "The order of hint in game level", example = "1")
	private int order;

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


	/**
	 * Gets order.
	 *
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Sets order.
	 *
	 * @param order the order
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public String toString() {
		return "HintImportDTO{" +
				"title='" + title + '\'' +
				", content='" + content + '\'' +
				", hintPenalty=" + hintPenalty +
				", order=" + order +
				'}';
	}
}
