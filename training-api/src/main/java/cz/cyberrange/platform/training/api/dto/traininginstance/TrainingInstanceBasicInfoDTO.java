package cz.cyberrange.platform.training.api.dto.traininginstance;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/** Encapsulates information about Training Instance */
@Data
@Schema(description = "A training instance as returned once its sandbox pool changed")
public class TrainingInstanceBasicInfoDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "2016-10-19T08:23:54.000Z")
  private LocalDateTime startTime;

  @Schema(example = "2017-10-19T08:23:54.000Z")
  private LocalDateTime endTime;

  @Schema(example = "Concluded Instance")
  private String title;

  /**
   * Carries the full token as stored on the instance, including the pin suffix the service appended
   * when it was generated. Returned only to the instance's organizers or an administrator, from the
   * pool assignment and unassignment endpoints.
   */
  @Schema(
      description = "Full access token, including the pin the server appended to it.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "hunter-6578")
  private String accessToken;

  /** Reflects the pool just assigned to, or removed from, the instance */
  @Schema(example = "1")
  private Long poolId;

  @Schema(
      description =
          "True when sandboxes come from the sandbox definition locally instead of from a pool.",
      example = "true")
  private boolean localEnvironment;

  @Schema(example = "1")
  private Long sandboxDefinitionId;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
  private boolean showStepperBar;

  @Schema(example = "true")
  private boolean backwardMode;
}
