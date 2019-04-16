package cz.muni.ics.kypo.training.api.dto.export;

public class FileToReturnDTO {
	private byte[] content;
	private String title;

	public FileToReturnDTO() {
	}

	public FileToReturnDTO(byte[] content, String title) {
		this.content = content;
		this.title = title;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override public String toString() {
		return "FileToReturnDTO{" + "content=" + content + ", title='" + title + '\'' + '}';
	}
}
