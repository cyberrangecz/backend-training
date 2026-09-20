package cz.cyberrange.platform.training.api.dto.export;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.enums.TRState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/** Encapsulates information about Training Run */
@Data
@Schema(description = "An exported training run, one trainee's pass through a training instance")
public class TrainingRunExportDTO {

  @Schema(
      description = "When the trainee started the run, in UTC",
      example = "2016-10-19T10:23:54Z")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime startTime;

  @Schema(description = "When the run ended, in UTC", example = "2022-10-19T10:23:54Z")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime endTime;

  private String eventLogReference;

  @Schema(example = "RUNNING")
  private TRState state;

  private UserRefExportDTO participantRef;
}
