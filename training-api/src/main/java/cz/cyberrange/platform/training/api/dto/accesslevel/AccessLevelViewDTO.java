package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The access level content shown to a participant currently running it. Omits the passkey that
 * {@link AccessLevelDTO} carries. Its {@code localContent} always reaches the participant with
 * runtime placeholders substituted (the training instance access token, a bearer token, the
 * participant's user reference id, the sandbox definition id and the central syslog address);
 * {@code cloudContent} is left exactly as authored.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(
    description = "An access level as shown to the participant running it, without the passkey.")
public class AccessLevelViewDTO extends AbstractLevelDTO {

  /**
   * Instructions for reaching the level's virtual machines from a cloud environment, as authored
   */
  @Schema(
      description = "Cloud connection instructions, exactly as authored.",
      example = "Connect using SSH config.")
  private String cloudContent;

  /**
   * Instructions for reaching the level's virtual machines from a local, non-cloud environment,
   * with its runtime placeholders already substituted for the requesting participant
   */
  @Schema(
      description = "Local connection instructions, with this run's placeholders filled in.",
      example = "Use vagrant SSH connection.")
  private String localContent;
}
