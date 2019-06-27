package cz.muni.ics.kypo.training.api.dto.imports;

/**
 * The type Info level import dto. * Encapsulates information about info level. Inherits from {@link AbstractLevelImportDTO}
 *
 * @author Boris Jadus(445343)
 */
public class InfoLevelImportDTO extends AbstractLevelImportDTO{

	private String content;

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

	@Override public String toString() {
		return "InfoLevelImportDTO{" + "content='" + content + '\'' + '}';
	}
}
