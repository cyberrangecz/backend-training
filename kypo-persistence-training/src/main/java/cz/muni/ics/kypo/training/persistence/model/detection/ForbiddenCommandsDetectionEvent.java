package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

@Entity
@Table(name = "forbidden_commands_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
        @NamedQuery(
                name = "ForbiddenCommandsDetectionEvent.findForbiddenCommandsEventById",
                query = "SELECT fcde FROM ForbiddenCommandsDetectionEvent fcde WHERE fcde.id = :eventId"
        ),
        @NamedQuery(
                name = "ForbiddenCommandsDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT fcde FROM ForbiddenCommandsDetectionEvent fcde WHERE fcde.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class ForbiddenCommandsDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "command_count", nullable = false)
    private int commandCount;
    @Column(name = "training_run_id", nullable = false)
    private Long trainingRunId;

    public int getCommandCount() {
        return commandCount;
    }

    public void setCommandCount(int commandCount) {
        this.commandCount = commandCount;
    }

    public Long getTrainingRunId() {
        return trainingRunId;
    }

    public void setTrainingRunId(Long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForbiddenCommandsDetectionEvent)) return false;
        if (!super.equals(o)) return false;
        ForbiddenCommandsDetectionEvent other = (ForbiddenCommandsDetectionEvent) o;
        return Objects.equals(getCommandCount(), other.getCommandCount()) &&
                Objects.equals(getTrainingRunId(), other.getTrainingRunId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getCommandCount());
    }

    @Override
    public String toString() {
        return "ForbiddenCommandsDetectionEvent{" +
                ", commandCount='" + commandCount +
                '}';
    }
}