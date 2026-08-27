package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A finding that several trainees submitted from the same network location, evidenced by the
 * address one of those submissions came from.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "LocationSimilarityDetectionEventDTO",
    description = "A detection event of type Location Similarity.",
    parent = AbstractDetectionEventDTO.class)
public class LocationSimilarityDetectionEventDTO extends AbstractDetectionEventDTO {

  @ApiModelProperty(value = "Ip address of participant.", example = "1.1.1.1")
  private String ipAddress;

  /**
   * Host name the address resolves back to, or the literal {@code unspecified} when it cannot be
   * resolved.
   */
  @ApiModelProperty(value = "DNS of participant.", example = "dns.provider.cz")
  private String dns;

  /**
   * Whether the resolved host name is the host this service itself runs on, which marks the shared
   * location as an artifact of the deployment rather than of the trainees. False whenever the
   * comparison could not be made at all.
   */
  @ApiModelProperty(value = "If the address is the same as deployment.", example = "false")
  private boolean isAddressDeploy;
}
