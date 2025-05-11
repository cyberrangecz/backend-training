package cz.cyberrange.platform.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCDeserializer;
import cz.cyberrange.platform.training.api.enums.TrainingType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Encapsulates information about Training Instance, intended for creation of new instance. */
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

  @ApiModelProperty(
      value = "AccessToken which will be modified and then used for accessing training run.",
      required = true,
      example = "hunter")
  @NotEmpty(message = "{traininginstancecreate.accessToken.NotEmpty.message}")
  private String accessToken;

  @ApiModelProperty(
      value = "Reference to training definition from which is training instance created.",
      required = true,
      example = "1")
  @NotNull(message = "{traininginstancecreate.trainingDefinition.NotNull.message}")
  private long trainingDefinitionId;

  @ApiModelProperty(value = "Id of sandbox pool assigned to training instance", example = "1")
  private Long poolId;

  @ApiModelProperty(
      value = "Indicates if local sandboxes are used for training runs.",
      example = "true")
  private boolean localEnvironment;

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

  @ApiModelProperty(
      value = "Maximum team size for cooperative training",
      example = "5",
      allowableValues = "1 to 12")
  private int maxTeamSize;

  @Getter
  @Setter
  @ApiModelProperty(
      value = "Type of training instance.",
      notes = "Defaults to LINEAR",
      example = "COOP")
  private TrainingType type = TrainingType.LINEAR;

  /**
   * Gets start time.
   *
   * @return the start time
   */
  public LocalDateTime getStartTime() {
    return startTime;
  }

  /**
   * Sets start time.
   *
   * @param startTime the start time
   */
  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  /**
   * Gets end time.
   *
   * @return the end time
   */
  public LocalDateTime getEndTime() {
    return endTime;
  }

  /**
   * Sets end time.
   *
   * @param endTime the end time
   */
  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  /**
   * Gets title.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets title.
   *
   * @param title the title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets access token.
   *
   * @return the access token
   */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * Sets access token.
   *
   * @param accessToken the access token
   */
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   * Gets training definition id.
   *
   * @return the training definition id
   */
  public long getTrainingDefinitionId() {
    return trainingDefinitionId;
  }

  /**
   * Sets training definition id.
   *
   * @param trainingDefinitionId the training definition id
   */
  public void setTrainingDefinitionId(long trainingDefinitionId) {
    this.trainingDefinitionId = trainingDefinitionId;
  }

  /**
   * Gets pool id.
   *
   * @return the pool id
   */
  public Long getPoolId() {
    return poolId;
  }

  /**
   * Sets pool id.
   *
   * @param poolId the pool id
   */
  public void setPoolId(Long poolId) {
    this.poolId = poolId;
  }

  /**
   * Gets if local environment (local sandboxes) is used for the training runs.
   *
   * @return true if local environment is enabled
   */
  public boolean isLocalEnvironment() {
    return localEnvironment;
  }

  /**
   * Sets if local environment (local sandboxes) is used for the training runs.
   *
   * @param localEnvironment true if local environment is enabled.
   */
  public void setLocalEnvironment(boolean localEnvironment) {
    this.localEnvironment = localEnvironment;
  }

  /**
   * Gets sandbox definition id.
   *
   * @return the sandbox definition id
   */
  public Long getSandboxDefinitionId() {
    return sandboxDefinitionId;
  }

  /**
   * Sets sandbox definition id.
   *
   * @param sandboxDefinitionId the sandbox definition id
   */
  public void setSandboxDefinitionId(Long sandboxDefinitionId) {
    this.sandboxDefinitionId = sandboxDefinitionId;
  }

  /**
   * Gets if stepper bar is shown while in run.
   *
   * @return true if bar is shown
   */
  public boolean isShowStepperBar() {
    return showStepperBar;
  }

  /**
   * Sets if stepper bar is shown while in run.
   *
   * @param showStepperBar true if bar is shown
   */
  public void setShowStepperBar(boolean showStepperBar) {
    this.showStepperBar = showStepperBar;
  }

  /**
   * Gets if trainee can during training run move back to the previous levels.
   *
   * @return true if backward mode is enabled.
   */
  public boolean isBackwardMode() {
    return backwardMode;
  }

  /**
   * Sets if trainee can during training run move back to the previous levels.
   *
   * @param backwardMode true if backward mode is enabled.
   */
  public void setBackwardMode(boolean backwardMode) {
    this.backwardMode = backwardMode;
  }

  @Override
  public String toString() {
    return "TrainingInstanceCreateDTO{"
        + "startTime="
        + startTime
        + ", endTime="
        + endTime
        + ", title='"
        + title
        + '\''
        + ", accessToken='"
        + accessToken
        + '\''
        + ", trainingDefinitionId="
        + trainingDefinitionId
        + ", poolId="
        + poolId
        + ", localEnvironment="
        + localEnvironment
        + ", showStepperBar="
        + showStepperBar
        + ", backwardMode="
        + backwardMode
        + '}';
  }
}
