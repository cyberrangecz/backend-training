package cz.cyberrange.platform.training.api.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about an info level. Inherits from {@link AbstractLevelImportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An info level to create, holding text the trainee only reads")
public class InfoLevelImportDTO extends AbstractLevelImportDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Informational stuff")
  @NotEmpty(message = "{infoLevel.content.NotEmpty.message}")
  private String content;
}
