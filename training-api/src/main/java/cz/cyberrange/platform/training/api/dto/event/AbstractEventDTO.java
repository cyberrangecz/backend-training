package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Common shape shared by a console command event and every training audit event. The {@code type}
 * property discriminates the concrete subtype during deserialization: the literal value {@code
 * COMMAND} selects {@link CommandEventDTO}, and any other value falls back to {@link
 * TrainingEventDTO}, whose own subtype registration further discriminates by the concrete training
 * event kind.
 */
@Data
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

  /**
   * The literal {@code COMMAND} for a console command, mapped from the fixed constant {@code
   * EventMapper} assigns it; for a training event, the {@code TYPE} constant declared on the audit
   * POJO the event was recorded as, copied through unchanged
   */
  @ApiModelProperty(value = "Type of the event")
  private String type;

  /**
   * For a console command, copied unchanged from an already-parsed {@code LocalDateTime}. For a
   * training event, converted by {@code EventMapper} from the epoch-millisecond value the audit
   * document stores, interpreted in the JVM's default time zone.
   */
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

  /**
   * For a console command, copied unchanged from an already-parsed {@code Duration}. For a training
   * event, converted by {@code EventMapper} from the elapsed-millisecond value the audit document
   * stores for time spent in the training run so far.
   */
  @ApiModelProperty(value = "Training time")
  @JsonProperty("training_time")
  private java.time.Duration trainingTime;
}
