package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.enums.TDState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about Training Definition including its authoring and lifecycle data */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "A training definition with its state and editing history.")
public class TrainingDefinitionDTO extends AbstractTrainingDefinitionDTO {

  private String[] prerequisites;

  private String[] outcomes;

  @Schema(example = "UNRELEASED")
  private TDState state;

  /** Primary key of the associated beta testing group entity, not a user id */
  @Schema(description = "Identifies the beta testing group, not any user.", example = "14")
  private Long betaTestingGroupId;

  /**
   * Left unset by the mapper; the facade always assigns it afterward, from whether any of the
   * definition's training instances still ends in the future
   */
  @Schema(
      description = "Whether none of the definition's training instances is still running.",
      example = "false")
  private boolean canBeArchived;

  /**
   * Overwritten with the current time by the service on every create or update, regardless of any
   * value supplied by the caller
   */
  @Schema(
      description = "Set by the server to the time of the last change.",
      example = "2017-10-19T10:23:54Z")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime lastEdited;

  /**
   * Overwritten with the current user's full name by the service on every create or update,
   * regardless of any value supplied by the caller
   */
  @Schema(
      description = "Set by the server to the name of the user who last saved it.",
      example = "John Doe")
  private String lastEditedBy;

  /**
   * Stamped once when the definition is created and carried over unchanged on every later update
   */
  @Schema(
      description = "Set by the server when the definition is created.",
      example = "2017-10-19T10:23:54Z")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime createdAt;
}
