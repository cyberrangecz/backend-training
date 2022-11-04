package cz.muni.ics.kypo.training.persistence.model.detection;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "no_commands_detection_event")
@PrimaryKeyJoinColumn(name = "id")
public class NoCommandsDetectionEvent extends AbstractDetectionEvent {

    @OneToMany(
            mappedBy = "detectionEvent",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<DetectionEventParticipant> participants = new HashSet<>();

    public Set<DetectionEventParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<DetectionEventParticipant> participants) {
        this.participants = participants;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoCommandsDetectionEvent)) return false;
        if (!super.equals(o)) return false;
        NoCommandsDetectionEvent other = (NoCommandsDetectionEvent) o;
        return Objects.equals(getParticipants(), other.getParticipants());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getParticipants());
    }

    @Override
    public String toString() {
        return "NoCommandsDetectionEvent{" +
                "participants='" + participants +
                '}';
    }
}
