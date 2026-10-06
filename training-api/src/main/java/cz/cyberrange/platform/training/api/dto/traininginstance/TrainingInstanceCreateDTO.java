package cz.cyberrange.platform.training.api.dto.traininginstance;

import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonDeserialize;

/** Encapsulates information about Training Instance, intended for creation of new instance */
@Getter
@Setter
@ToString
@Schema(description = "A new training instance to schedule for a training definition")
public class TrainingInstanceCreateDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2020-11-20T10:28:02.727Z")
  @NotNull(message = "{trainingInstance.startTime.NotNull.message}")
  @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
  private LocalDateTime startTime;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2020-11-25T10:26:02.727Z")
  @NotNull(message = "{traininginstancecreate.endTime.NotNull.message}")
  @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
  private LocalDateTime endTime;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "December instance")
  @NotEmpty(message = "{traininginstancecreate.title.NotEmpty.message}")
  private String title;

  /**
   * Trimmed and used as a prefix: the service appends a generated pin to it to form the actual
   * stored access token
   */
  @Schema(
      description = "Prefix of the access token; the server appends a generated pin to it.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "hunter")
  @NotEmpty(message = "{traininginstancecreate.accessToken.NotEmpty.message}")
  private String accessToken;

  /**
   * Primary key of the training definition to base the instance on; resolved by the facade, not the
   * mapper
   */
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  @NotNull(message = "{traininginstancecreate.trainingDefinition.NotNull.message}")
  private long trainingDefinitionId;

  /**
   * Mutually exclusive with localEnvironment: rejected when localEnvironment is true, required when
   * it is false. When given, the facade locks the pool with the instance's generated access token
   * once the instance is created.
   */
  @Schema(example = "1")
  private Long poolId;

  @Schema(
      description =
          "True when sandboxes come from the sandbox definition locally instead of from a pool.",
      example = "true")
  private boolean localEnvironment;

  /**
   * Rejected when localEnvironment is false. Identifies the sandbox definition later used to create
   * each participant's local sandbox for a training run of this instance.
   */
  @Schema(example = "1")
  private Long sandboxDefinitionId;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
  private boolean showStepperBar;

  @Schema(example = "true")
  private boolean backwardMode;

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public long getTrainingDefinitionId() {
    return trainingDefinitionId;
  }

  public void setTrainingDefinitionId(long trainingDefinitionId) {
    this.trainingDefinitionId = trainingDefinitionId;
  }

  public Long getPoolId() {
    return poolId;
  }

  public void setPoolId(Long poolId) {
    this.poolId = poolId;
  }

  public boolean isLocalEnvironment() {
    return localEnvironment;
  }

  public void setLocalEnvironment(boolean localEnvironment) {
    this.localEnvironment = localEnvironment;
  }

  public Long getSandboxDefinitionId() {
    return sandboxDefinitionId;
  }

  public void setSandboxDefinitionId(Long sandboxDefinitionId) {
    this.sandboxDefinitionId = sandboxDefinitionId;
  }

  public boolean isShowStepperBar() {
    return showStepperBar;
  }

  public void setShowStepperBar(boolean showStepperBar) {
    this.showStepperBar = showStepperBar;
  }

  public boolean isBackwardMode() {
    return backwardMode;
  }

  public void setBackwardMode(boolean backwardMode) {
    this.backwardMode = backwardMode;
  }
}
