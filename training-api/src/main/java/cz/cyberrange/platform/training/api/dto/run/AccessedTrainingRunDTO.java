package cz.cyberrange.platform.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.enums.Actions;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/** Encapsulates information about already accessed training run */
@Data
@Schema(description = "A training run the caller has taken, summarised for their own list of runs")
public class AccessedTrainingRunDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "Concluded Instance")
  private String title;

  @Schema(example = "2016-10-19T10:23:54")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime trainingInstanceStartDate;

  @Schema(example = "2017-10-19T10:23:54")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime trainingInstanceEndDate;

  /** One-based position of the current level, the stored zero-based level order plus one */
  @Schema(description = "Position of the run's current level, counting from one.", example = "1")
  private int currentLevelOrder;

  /** Count of levels in the training definition, the highest stored level order plus one */
  @Schema(example = "3")
  private int numberOfLevels;

  /**
   * {@code RESULTS} once the run is finished or its training instance has ended, {@code RESUME}
   * otherwise
   */
  @Schema(
      description =
          "RESULTS once the run is finished or its training instance has ended, RESUME otherwise.",
      example = "RESULTS")
  private Actions possibleAction;

  @Schema(example = "1")
  private Long instanceId;
}
