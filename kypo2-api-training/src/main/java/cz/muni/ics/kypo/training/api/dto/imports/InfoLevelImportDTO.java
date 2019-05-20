package cz.muni.ics.kypo.training.api.dto.imports;
/**
 * @author Boris Jadus(445343)
 */
public class InfoLevelImportDTO extends AbstractLevelImportDTO{

	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override public String toString() {
		return "InfoLevelImportDTO{" + "content='" + content + '\'' + '}';
	}
}
