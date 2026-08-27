package cz.cyberrange.platform.training.persistence.model.detection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "location_similarity_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
  @NamedQuery(
      name = "LocationSimilarityDetectionEvent.findLocationSimilarityEventById",
      query = "SELECT lsde FROM LocationSimilarityDetectionEvent lsde WHERE lsde.id = :eventId"),
  @NamedQuery(
      name = "LocationSimilarityDetectionEvent.findAllByCheatingDetectionId",
      query =
          "SELECT lsde FROM LocationSimilarityDetectionEvent lsde WHERE lsde.cheatingDetectionId = :cheatingDetectionId")
})
/**
 * A finding that several trainees submitted from the same network location, evidenced by the
 * address one of those submissions came from.
 */
public class LocationSimilarityDetectionEvent extends AbstractDetectionEvent {

  @Column(name = "ip_address")
  private String ipAddress;

  /** Host name the address resolves back to, or {@code unspecified} when it cannot be resolved. */
  @Column(name = "dns")
  private String dns;

  /**
   * Whether the resolved host is the one this service runs on, which explains the shared location
   * as an artifact of the deployment. False whenever the comparison could not be made.
   */
  @Column(name = "is_address_deploy")
  private boolean isAddressDeploy;
}
