package cz.muni.ics.kypo.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Class encapsulating entity into file.
 */
@ApiModel(value = "FileToReturnDTO", description = "Wrapping model which contains the content and title of the file.")
public class FileToReturnDTO {

	@ApiModelProperty(value = "Content of the file.", example = "[string]")
	private byte[] content;
	@ApiModelProperty(value = "Title of the file.", example = "TrainingInstance-NetworkDemolition")
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
