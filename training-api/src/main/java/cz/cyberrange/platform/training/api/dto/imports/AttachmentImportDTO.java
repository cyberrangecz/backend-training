package cz.cyberrange.platform.training.api.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/** An imported attachment, carrying the URL to its file or website content */
@Data
@Schema(description = "An attachment to create on a training level")
public class AttachmentImportDTO {
  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description = "URL of the attached file or website")
  @NotEmpty(message = "{attachment.content.NotEmpty.message}")
  private String content;
}
