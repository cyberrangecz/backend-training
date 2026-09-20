package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Class encapsulating entity into file */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A generated file, its bytes and the name it is offered under")
public class FileToReturnDTO {

  @Schema(description = "The file's bytes.")
  private byte[] content;

  @Schema(
      description = "Name the file is offered under, without the extension",
      example = "TrainingInstance-NetworkDemolition")
  private String title;
}
