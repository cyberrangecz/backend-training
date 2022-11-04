package cz.muni.ics.kypo.training.persistence.model.detection;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "minimal_solve_time_detection_event")
@PrimaryKeyJoinColumn(name = "id")
public class MinimalSolveTimeDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "minimal_solve_time")
    private Long minimalSolveTime;

    @OneToMany(
            mappedBy = "detectionEvent",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<DetectionEventParticipant> participants = new HashSet<>();

    public Long getMinimalSolveTime() {
        return minimalSolveTime;
    }

    public void setMinimalSolveTime(Long minimalSolveTime) {
        this.minimalSolveTime = minimalSolveTime;
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
        if (!(o instanceof MinimalSolveTimeDetectionEvent)) return false;
        if (!super.equals(o)) return false;
        MinimalSolveTimeDetectionEvent other = (MinimalSolveTimeDetectionEvent) o;
        return Objects.equals(getMinimalSolveTime(), other.getMinimalSolveTime()) &&
                Objects.equals(getParticipants(), other.getParticipants());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMinimalSolveTime(), getParticipants());
    }

    @Override
    public String toString() {
        return "MinimalSolveTimeDetectionEvent{" +
                "minimalSolveTime='" + minimalSolveTime + '\'' +
                ", participants='" + participants +
                '}';
    }
}
