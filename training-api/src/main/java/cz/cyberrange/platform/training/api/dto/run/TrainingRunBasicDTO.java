package cz.cyberrange.platform.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.enums.TRState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/** The shape of a training run returned to both organizers and trainees of that run */
@Data
@Schema(description = "A training run as both its organizers and its trainee may see it")
public class TrainingRunBasicDTO {
  @Schema(example = "1")
  protected Long id;

  @Schema(example = "ALLOCATED")
  protected TRState state;

  @Schema(example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  protected LocalDateTime startTime;

  @Schema(example = "2022-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  protected LocalDateTime endTime;

  protected UserRefDTO participantRef;

  protected String sandboxInstanceRefId;

  @Schema(example = "1")
  protected Long trainingInstanceId;

  @Schema(example = "1")
  protected Long trainingDefinitionId;

  @Schema(example = "1")
  protected Long currentLevelId;

  @Schema(description = "Position of the run's current level, counting from zero.", example = "1")
  protected Integer currentLevelOrder;
}
