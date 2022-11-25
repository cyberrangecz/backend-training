package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "forbidden_commands_detection_event")
@PrimaryKeyJoinColumn(name = "id")
public class ForbiddenCommandsDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "forbidden_commands", nullable = false)
    private String[] forbiddenCommands;
    @JoinColumn(name = "participant_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private DetectionEventParticipant participant;

    public String[] getForbiddenCommands() {
        return forbiddenCommands;
    }

    public void setForbiddenCommands(String[] forbiddenCommands) {
        this.forbiddenCommands = forbiddenCommands;
    }

    public DetectionEventParticipant getParticipant() {
        return participant;
    }

    public void setParticipant(DetectionEventParticipant participant) {
        this.participant = participant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForbiddenCommandsDetectionEvent)) return false;
        if (!super.equals(o)) return false;
        ForbiddenCommandsDetectionEvent other = (ForbiddenCommandsDetectionEvent) o;
        return Objects.equals(getParticipant(), other.getParticipant()) &&
                Objects.equals(getForbiddenCommands(), other.getForbiddenCommands());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getParticipant(), getForbiddenCommands());
    }

    @Override
    public String toString() {
        return "ForbiddenCommandsDetectionEvent{" +
                "participant='" + participant + '\'' +
                ", forbiddenCommands='" + forbiddenCommands +
                '}';
    }
}
