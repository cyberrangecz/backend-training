package cz.muni.ics.kypo.training.persistence.model.detection;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "time_proximity_detection_event")
@PrimaryKeyJoinColumn(name = "id")
public class TimeProximityDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "threshold")
    private Long threshold;
    @OneToMany(
            mappedBy = "detectionEvent",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<DetectionEventParticipant> participants = new HashSet<>();

    public Long getThreshold() {
        return threshold;
    }

    public void setThreshold(Long threshold) {
        this.threshold = threshold;
    }

    public Set<DetectionEventParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<DetectionEventParticipant> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeProximityDetectionEvent)) return false;
        if (!super.equals(o)) return false;
        TimeProximityDetectionEvent other = (TimeProximityDetectionEvent) o;
        return Objects.equals(getParticipants(), other.getParticipants()) &&
                Objects.equals(getThreshold(), other.getThreshold());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getParticipants(), getThreshold());
    }

    @Override
    public String toString() {
        return "TimeProximityDetectionEvent{" +
                "participants='" + participants + '\'' +
                ", threshold='" + threshold +
                '}';
    }
}
