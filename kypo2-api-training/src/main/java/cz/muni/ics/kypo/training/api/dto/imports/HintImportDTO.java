package cz.muni.ics.kypo.training.api.dto.imports;

/**
 *  Encapsulates information about Hint.
 *
 * @author Boris Jadus(445343)
 */
public class HintImportDTO {

	private String title;
	private String content;
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
