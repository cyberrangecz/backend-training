package cz.cyberrange.platform.training.api.dto.archive;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Snapshot of one finished training instance, written as a single JSON file into its archive */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A finished training instance as recorded in its archive")
public class TrainingInstanceArchiveDTO {

  @Schema(example = "1")
  private Long id;

  /** Primary key of the training definition this instance was created from */
  @Schema(example = "1")
  private Long definitionId;

  @Schema(example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime startTime;

  @Schema(example = "2017-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime endTime;

  @Schema(example = "Concluded Instance")
  private String title;

  /**
   * User reference ids ({@code UserRef.userRefId}) of the organizers of this training instance, not
   * their local primary keys
   */
  private Set<Long> organizersRefIds;

  @Schema(example = "pass-1234")
  private String accessToken;

  @Schema(
      description =
          "True when sandboxes came from the sandbox definition locally instead of from a pool.",
      example = "true")
  private boolean localEnvironment;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
  private boolean showStepperBar;

  @Schema(example = "true")
  private boolean backwardMode;
}
