package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about info level. Inherits from {@link AbstractLevelExportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An exported info level, holding text the trainee only reads")
public class InfoLevelExportDTO extends AbstractLevelExportDTO {

  @Schema(example = "Informational stuff")
  private String content;

  /** Sets {@link #content} to an empty string, the value kept when a mapped source has none */
  public InfoLevelExportDTO() {
    this.content = "";
  }
}
