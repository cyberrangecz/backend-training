package cz.cyberrange.platform.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Data;

/** Contains generally safe, descriptive-only data accessible by both organizers and trainees. */
@Data
@ApiModel(
    value = "TrainingInstanceBasicDTO",
    description =
        "A session of attending a concrete training, which involves a deployment of the training definition in one or more sandbox instances that are then assigned to participants. The instance comprises one or more training runs.")
public class TrainingInstanceBasicDTO {

  @ApiModelProperty(value = "Main identifier of training instance.", example = "1")
  protected Long id;

  @ApiModelProperty(
      value = "Date when training instance starts.",
      example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  protected LocalDateTime startTime;

  @ApiModelProperty(value = "Date when training instance ends.", example = "2017-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  protected LocalDateTime endTime;

  @ApiModelProperty(
      value = "Short textual description of the training instance.",
      example = "Concluded Instance")
  protected String title;

  /**
   * Primary key of the associated training definition, mapped from the instance's
   * trainingDefinition relation. This DTO carries no accessToken field; it is returned to instance
   * organizers and to its trainee participants alike.
   */
  @ApiModelProperty(value = "ID of set training instance", example = "1")
  protected Long definitionId;
}
