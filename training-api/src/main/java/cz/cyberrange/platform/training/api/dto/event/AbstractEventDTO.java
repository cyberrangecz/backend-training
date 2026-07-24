package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(
    value = "AbstractEventDTO",
    description = "Parent class for all event DTOs (training events and commands)",
    subTypes = {CommandEventDTO.class, TrainingEventDTO.class})
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true,
    defaultImpl = TrainingEventDTO.class)
@JsonSubTypes({@JsonSubTypes.Type(value = CommandEventDTO.class, name = "COMMAND")})
public abstract class AbstractEventDTO {

  @ApiModelProperty(value = "OpenSearch document identifier of the event")
  @JsonProperty("event_id")
  private String eventId;

  @ApiModelProperty(value = "Type of the event")
  private String type;

  @ApiModelProperty(value = "Timestamp of the event", example = "2021-03-24T12:00:00")
  private LocalDateTime timestamp;

  @ApiModelProperty(
      value =
          "Sandbox identifier. Plain sandbox UUID for administrators and organizers of the training"
              + " instance; for other callers, plain only for their own run's sandbox and a SHA-256"
              + " hash otherwise.",
      example = "db5c9da0-e2d9-482a-a924-a12def8ac2ef")
  @JsonProperty("sandbox_id")
  private String sandboxId;

  @ApiModelProperty(value = "Training time")
  @JsonProperty("training_time")
  private java.time.Duration trainingTime;
}
