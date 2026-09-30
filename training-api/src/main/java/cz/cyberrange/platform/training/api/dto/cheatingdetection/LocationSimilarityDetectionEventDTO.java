package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A finding that several trainees submitted from the same network location, evidenced by the
 * address one of those submissions came from
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "A finding that several trainees submitted from the same network location.")
public class LocationSimilarityDetectionEventDTO extends AbstractDetectionEventDTO {

  @Schema(example = "1.1.1.1", description = "Address one of the grouped submissions came from.")
  private String ipAddress;

  /**
   * Host name the address resolves back to, or the literal {@code unspecified} when it cannot be
   * resolved
   */
  @Schema(
      example = "dns.provider.cz",
      description = "Host name the address resolves to, or unspecified when it does not resolve.")
  private String dns;

  /**
   * Whether the resolved host name is the host this service itself runs on, which marks the shared
   * location as an artifact of the deployment rather than of the trainees. False whenever the
   * comparison could not be made at all.
   */
  @Schema(
      example = "false",
      description = "Whether the resolved host is the one this service runs on.")
  private boolean isAddressDeploy;
}
