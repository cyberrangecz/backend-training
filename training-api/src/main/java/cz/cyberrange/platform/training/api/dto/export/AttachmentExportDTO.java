package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** An exported training level attachment, carrying the URL to its file or website content */
@Data
@Schema(description = "An exported attachment of a training level, holding the URL of its content")
public class AttachmentExportDTO {

  private String content;
}
