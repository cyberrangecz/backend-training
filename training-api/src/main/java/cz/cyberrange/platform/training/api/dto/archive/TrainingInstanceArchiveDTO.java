package cz.cyberrange.platform.training.api.dto.archive;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Snapshot of one finished training instance, written as a single JSON file into its archive. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(
    value = "TrainingInstanceArchiveDTO",
    description =
        "The finished and archived instance of training definition which includes individual finished training runs of participants.")
public class TrainingInstanceArchiveDTO {

  @ApiModelProperty(value = "Main identifier of training instance.", example = "1")
  private Long id;

  /** Primary key of the training definition this instance was created from. */
  @ApiModelProperty(
      value = "Main identifier of training definition associated with this instance.",
      example = "1")
  private Long definitionId;

  @ApiModelProperty(
      value = "Date when training instance starts.",
      example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime startTime;

  @ApiModelProperty(value = "Date when training instance ends.", example = "2017-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime endTime;

  @ApiModelProperty(
      value = "Short textual description of the training instance.",
      example = "Concluded Instance")
  private String title;

  /**
   * User reference ids ({@code UserRef.userRefId}) of the organizers of this training instance, not
   * their local primary keys.
   */
  @ApiModelProperty(value = "Reference to organizersRefIds which organize training instance.")
  private Set<Long> organizersRefIds;

  @ApiModelProperty(
      value = "Token needed to access runs created from this definition",
      example = "pass-1234")
  private String accessToken;

  @ApiModelProperty(
      value = "Indicates if local sandboxes are used for training runs.",
      example = "true")
  private boolean localEnvironment;

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
}
