package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotEmpty;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiModel(value = "InfoLevelDTO", description = ".")
public class InfoLevelDTO extends AbstractLevelDTO {

	@NotEmpty(message = "Level content cannot be empty")
	private String content;

	public InfoLevelDTO() {}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "InfoLevelDTO [content=" + content + ", toString()=" + super.toString() + "]";
	}

}
