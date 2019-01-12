package cz.muni.ics.kypo.training.api.dto.imports;

public class HintImportDTO {

	private String title;
	private String content;
	private Integer hintPenalty;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getHintPenalty() {
		return hintPenalty;
	}

	public void setHintPenalty(Integer hintPenalty) {
		this.hintPenalty = hintPenalty;
	}

	@Override public String toString() {
		return "HintImportDTO{" + "title='" + title + '\'' + ", content='" + content + '\'' + ", hintPenalty=" + hintPenalty + '}';
	}
}
