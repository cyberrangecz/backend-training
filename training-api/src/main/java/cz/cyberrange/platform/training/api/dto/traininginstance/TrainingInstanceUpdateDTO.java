package cz.cyberrange.platform.training.api.dto.traininginstance;

import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;

/** Encapsulates information about Training Instance, intended for edit of the instance */
@Data
@Schema(description = "The full set of values to store on an existing training instance")
public class TrainingInstanceUpdateDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
  @NotNull(message = "{traininginstanceupdate.id.NotNull.message}")
  private Long id;

  /**
   * Changing it once the instance is running or finished is refused; it must also not be after
   * {@code endTime}
   */
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2019-10-19T10:28:02.727Z")
  @NotNull(message = "{traininginstanceupdate.startTime.NotNull.message}")
  @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
  private LocalDateTime startTime;

  /**
   * Must not be after {@code startTime}. Moving it into the future on an instance that has already
   * ended is refused, since that would bring an expired instance back to life; moving it further
   * into the past leaves the instance ended and is allowed.
   */
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2019-10-25T10:28:02.727Z")
  @NotNull(message = "{traininginstanceupdate.endTime.NotNull.message}")
  @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
  private LocalDateTime endTime;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Current Instance")
  @NotEmpty(message = "{traininginstanceupdate.title.NotEmpty.message}")
  private String title;

  /**
   * Compared, with its generated pin stripped, against the instance's current access token. If the
   * instance has not started yet, a value that differs from that stripped token causes a new pin to
   * be generated and appended; if the instance is running or finished, any such change is refused.
   * Otherwise the stored token is kept unchanged.
   */
  @Schema(
      description = "Changing it before the instance starts makes the server generate a new pin.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "hello-6578")
  @NotEmpty(message = "{traininginstanceupdate.accessToken.NotEmpty.message}")
  private String accessToken;

  /**
   * Primary key of the training definition to associate with the instance; read directly by the
   * facade, not through the mapper. Changing it once the instance has started is refused.
   */
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  @NotNull(message = "{traininginstanceupdate.trainingDefinition.NotNull.message}")
  private Long trainingDefinitionId;

  /** Changing it once the instance is running or finished is refused */
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
