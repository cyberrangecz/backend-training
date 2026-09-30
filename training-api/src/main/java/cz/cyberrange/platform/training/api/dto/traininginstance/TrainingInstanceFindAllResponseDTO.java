package cz.cyberrange.platform.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Encapsulates basic information about Training Instance */
@Data
@NoArgsConstructor
public class TrainingInstanceFindAllResponseDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime startTime;

  @Schema(example = "2017-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime endTime;

  @Schema(example = "Concluded Instance")
  private String title;

  /**
   * Mapped through {@code TrainingDefinitionMapper.mapToDTO}; its {@code canBeArchived} is never
   * patched afterward here, so it always carries that flag's default value of false
   */
  private TrainingDefinitionDTO trainingDefinition;

  /**
   * Carries the full token as stored on the instance, including the pin suffix the service appended
   * when it was generated. Returned only from the administrator- and organizer-only listing
   * endpoint.
   */
  @Schema(
      description = "Full access token, including the pin the server appended to it.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "hunter-6578")
  private String accessToken;

  @Schema(example = "1")
  private Long poolId;

  @Schema(example = "2017-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime lastEdited;

  @Schema(example = "John Doe")
  private String lastEditedBy;

  @Schema(
      description =
          "True when sandboxes come from the sandbox definition locally instead of from a pool.",
      example = "true")
  private boolean localEnvironment;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
  private boolean showStepperBar;

  @Schema(example = "true")
  private boolean backwardMode;
}
