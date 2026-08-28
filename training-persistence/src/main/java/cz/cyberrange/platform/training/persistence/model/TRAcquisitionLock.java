package cz.cyberrange.platform.training.persistence.model;

import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Marks that one participant's request to access a training instance is in flight, so that a second
 * concurrent request for the same participant and instance fails instead of creating a second
 * training run. The unique constraint on participant and instance is what makes a duplicate insert
 * fail; the caller that hits that failure surfaces it as a request to resume the existing run
 * rather than start another one. The row is deleted once the attempt resolves: by the run reaching
 * a finished or archived state, by the run being deleted, or by the calling request failing
 * outright.
 */
@Getter
@Setter
@Entity
@Table(
    name = "training_run_acquisition_lock",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"participant_ref_id", "training_instance_id"}))
@NamedQueries({
  @NamedQuery(
      name = "TRAcquisitionLock.deleteByParticipantRefIdAndTrainingInstanceId",
      query =
          "DELETE FROM TRAcquisitionLock tral WHERE tral.participantRefId = :participantRefId AND tral.trainingInstanceId = :trainingInstanceId")
})
public class TRAcquisitionLock extends AbstractEntity<Long> {

  // Holds the external, cross-service user identifier of the participant, not the local UserRef
  // primary key.
  @Column(name = "participant_ref_id")
  private Long participantRefId;

  @Column(name = "training_instance_id")
  private Long trainingInstanceId;

  @Column(name = "creation_time")
  private LocalDateTime creationTime;

  public TRAcquisitionLock() {}

  /**
   * Creates a lock row scoped to one participant's access attempt on one training instance.
   *
   * @param participantRefId the external user-and-group identifier of the participant.
   * @param trainingInstanceId the identifier of the training instance being accessed.
   * @param creationTime the time the attempt started.
   */
  public TRAcquisitionLock(
      Long participantRefId, Long trainingInstanceId, LocalDateTime creationTime) {
    this.participantRefId = participantRefId;
    this.trainingInstanceId = trainingInstanceId;
    this.creationTime = creationTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TRAcquisitionLock)) return false;
    TRAcquisitionLock that = (TRAcquisitionLock) o;
    return Objects.equals(getParticipantRefId(), that.getParticipantRefId())
        && Objects.equals(getTrainingInstanceId(), that.getTrainingInstanceId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getParticipantRefId(), getTrainingInstanceId());
  }

  @Override
  public String toString() {
    return "TRAcquisitionLock{"
        + "id="
        + super.getId()
        + ", participantRefId="
        + participantRefId
        + ", trainingInstanceId="
        + trainingInstanceId
        + ", creationTime="
        + creationTime
        + '}';
  }
}
