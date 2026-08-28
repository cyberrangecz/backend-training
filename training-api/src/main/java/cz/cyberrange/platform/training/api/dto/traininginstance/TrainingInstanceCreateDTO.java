package cz.cyberrange.platform.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Encapsulates information about Training Instance, intended for creation of new instance */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingInstanceCreateDTO", description = "Training Instance to create.")
public class TrainingInstanceCreateDTO {

  @ApiModelProperty(
      value = "Date when training instance starts.",
      required = true,
      example = "2020-11-20T10:28:02.727Z")
  @NotNull(message = "{trainingInstance.startTime.NotNull.message}")
  @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
  private LocalDateTime startTime;

  @ApiModelProperty(
      value = "Date when training instance ends.",
      required = true,
      example = "2020-11-25T10:26:02.727Z")
  @NotNull(message = "{traininginstancecreate.endTime.NotNull.message}")
  @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
  private LocalDateTime endTime;

  @ApiModelProperty(
      value = "Short textual description of the training instance.",
      required = true,
      example = "December instance")
  @NotEmpty(message = "{traininginstancecreate.title.NotEmpty.message}")
  private String title;

  /**
   * Trimmed and used as a prefix: the service appends a generated pin to it to form the actual
   * stored access token
   */
  @ApiModelProperty(
      value = "AccessToken which will be modified and then used for accessing training run.",
      required = true,
      example = "hunter")
  @NotEmpty(message = "{traininginstancecreate.accessToken.NotEmpty.message}")
  private String accessToken;

  /**
   * Primary key of the training definition to base the instance on; resolved by the facade, not the
   * mapper
   */
  @ApiModelProperty(
      value = "Reference to training definition from which is training instance created.",
      required = true,
      example = "1")
  @NotNull(message = "{traininginstancecreate.trainingDefinition.NotNull.message}")
  private long trainingDefinitionId;

  /**
   * Mutually exclusive with localEnvironment: rejected when localEnvironment is true, required when
   * it is false. When given, the facade locks the pool with the instance's generated access token
   * once the instance is created.
   */
  @ApiModelProperty(value = "Id of sandbox pool assigned to training instance", example = "1")
  private Long poolId;

  @ApiModelProperty(
      value = "Indicates if local sandboxes are used for training runs.",
      example = "true")
  private boolean localEnvironment;

  /**
   * Rejected when localEnvironment is false. Identifies the sandbox definition later used to create
   * each participant's local sandbox for a training run of this instance.
   */
  @ApiModelProperty(value = "Id of sandbox definition assigned to training instance", example = "1")
  private Long sandboxDefinitionId;

  @ApiModelProperty(
      value = "Sign if stepper bar should be displayed.",
      required = true,
      example = "true")
  private boolean showStepperBar;

  @ApiModelProperty(
      value =
          "Indicates if trainee can during training run move to the previous already solved levels.",
      example = "true")
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
