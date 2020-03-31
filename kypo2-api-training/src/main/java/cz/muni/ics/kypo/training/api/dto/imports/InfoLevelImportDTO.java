package cz.muni.ics.kypo.training.api.dto.imports;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

/**
 * The type Info level import dto. * Encapsulates information about info level. Inherits from {@link AbstractLevelImportDTO}
 *
 */
@ApiModel(value = "InfoLevelImportDTO", description = "An imported info level.", parent = AbstractLevelImportDTO.class)
public class InfoLevelImportDTO extends AbstractLevelImportDTO{

	@ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Informational stuff")
	@NotEmpty(message = "{infolevelimport.content.NotEmpty.message}")
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
