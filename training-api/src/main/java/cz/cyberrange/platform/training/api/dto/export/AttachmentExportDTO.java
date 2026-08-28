package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** An exported training level attachment, carrying the URL to its file or website content */
@Data
@ApiModel(value = "AttachmentExportDTO", description = "An exported attachment of training level.")
public class AttachmentExportDTO {

  @ApiModelProperty(value = "URL link to file or website.")
  private String content;
}
