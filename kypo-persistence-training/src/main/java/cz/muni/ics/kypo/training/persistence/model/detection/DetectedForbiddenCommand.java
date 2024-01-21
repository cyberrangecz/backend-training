package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;
import cz.muni.ics.kypo.training.persistence.model.enums.CommandType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "detected_forbidden_command")
public class DetectedForbiddenCommand extends AbstractEntity<Long> {

    @Column(name = "command", nullable = false)
    private String command;
    @Column(name = "command_type", nullable = false)
    private CommandType type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_event_id")
    private ForbiddenCommandsDetectionEvent detectionEvent;
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectedForbiddenCommand that = (DetectedForbiddenCommand) o;
        return Objects.equals(command, that.command) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, type);
    }

    @Override
    public String toString() {
        return "DetectedForbiddenCommand{" +
                "command='" + command + '\'' +
                ", type=" + type +
                '}';
    }
}
