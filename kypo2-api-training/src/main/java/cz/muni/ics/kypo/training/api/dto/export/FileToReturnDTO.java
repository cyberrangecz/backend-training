package cz.muni.ics.kypo.training.api.dto.export;

/**
 * Class encapsulating entity into file.
 */
public class FileToReturnDTO {
	private byte[] content;
	private String title;

	/**
	 * Instantiates a new File to return dto.
	 */
	public FileToReturnDTO() {
	}

	/**
	 * Instantiates a new File to return dto.
	 *
	 * @param content the content in byte array
	 * @param title   the title of the file
	 */
	public FileToReturnDTO(byte[] content, String title) {
		this.content = content;
		this.title = title;
	}

	/**
	 * Get content in byte array
	 *
	 * @return the in byte array
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * Sets content.
	 *
	 * @param content the content
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}

	/**
	 * Gets title of the file.
	 *
	 * @return the title of the file.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets title of the file.
	 *
	 * @param title the title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	@Override public String toString() {
		return "FileToReturnDTO{" + "content=" + content + ", title='" + title + '\'' + '}';
	}
}
