package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Full representation of an access level, reachable only by a designer, organizer or administrator
 * viewing or editing a training definition's levels, never by a participant running it. Carries the
 * passkey and the connection content as authored, before the passkey removal and placeholder
 * substitution {@link AccessLevelViewDTO} applies for a running participant.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An access level as authored, with its passkey and connection details.")
public class AccessLevelDTO extends AbstractLevelDTO {

  /** The value a participant must submit to complete the level, compared to it verbatim */
  @Schema(
      description = "The value a participant must submit to finish the level.",
      example = "secretAnswer")
  private String passkey;

  /**
   * Instructions for reaching the level's virtual machines from a cloud environment, as authored
   */
  @Schema(
      description = "How to reach the level's virtual machines from a cloud environment.",
      example = "Connect using SSH config.")
  private String cloudContent;

  /**
   * Instructions for reaching the level's virtual machines from a local, non-cloud environment, as
   * authored
   */
  @Schema(
      description = "How to reach the level's virtual machines from a local environment.",
      example = "Use vagrant SSH connection.")
  private String localContent;
}
