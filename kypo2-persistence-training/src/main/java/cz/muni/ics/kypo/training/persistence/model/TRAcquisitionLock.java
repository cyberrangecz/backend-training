package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The entity which prevents multiple training runs to be created in parallel threads. Basically it determines active training runs.
 *
 * @author Dominik Pilar
 */
@Entity(name = "TrainingRunAcquisitionLock")
@Table(name = "training_run_acquisition_lock", uniqueConstraints = @UniqueConstraint(columnNames = {"participant_ref_id", "training_instance_id"}))
public class TRAcquisitionLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "participant_ref_id")
    private Long participantRefId;
    @Column(name = "training_instance_id")
    private Long trainingInstanceId;
    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    public TRAcquisitionLock() {
    }

    public TRAcquisitionLock(Long participantRefId, Long trainingInstanceId, LocalDateTime creationTime) {
        this.participantRefId = participantRefId;
        this.trainingInstanceId = trainingInstanceId;
        this.creationTime = creationTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParticipantRefId() {
        return participantRefId;
    }

    public void setParticipantRefId(Long participantRefId) {
        this.participantRefId = participantRefId;
    }

    public Long getTrainingInstanceId() {
        return trainingInstanceId;
    }

    public void setTrainingInstanceId(Long trainingInstanceId) {
        this.trainingInstanceId = trainingInstanceId;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TRAcquisitionLock))
            return false;
        TRAcquisitionLock that = (TRAcquisitionLock) o;
        return Objects.equals(getParticipantRefId(), that.getParticipantRefId()) &&
                Objects.equals(getTrainingInstanceId(), that.getTrainingInstanceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParticipantRefId(), getTrainingInstanceId());
    }

    @Override
    public String toString() {
        return "TRAcquisitionLock{" +
                "id=" + id +
                ", participantRefId=" + participantRefId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", creationTime=" + creationTime +
                '}';
    }
}
