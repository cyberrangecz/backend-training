package cz.cyberrange.platform.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/** Contains generally safe, descriptive-only data accessible by both organizers and trainees */
@Data
@Schema(description = "A training instance as both its organizers and its trainees may see it")
public class TrainingInstanceBasicDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  protected Long id;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  protected LocalDateTime startTime;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2017-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  protected LocalDateTime endTime;

  @Schema(example = "Concluded Instance")
  protected String title;

  /**
   * Primary key of the associated training definition, mapped from the instance's
   * trainingDefinition relation. This DTO carries no accessToken field; it is returned to instance
   * organizers and to its trainee participants alike.
   */
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  protected Long definitionId;
}
