package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import cz.muni.ics.kypo.training.api.enums.CommandType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about forbidden command.
 */
@ApiModel(value = "DetectedForbiddenCommandDTO", description = "Basic information about detected forbidden command.")
public class DetectedForbiddenCommandDTO {

    @ApiModelProperty(value = "command.", example = "nmap")
    private String command;
    @ApiModelProperty(value = "Type of command.", example = "BASH")
    private CommandType type;

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
        DetectedForbiddenCommandDTO that = (DetectedForbiddenCommandDTO) o;
        return Objects.equals(command, that.command) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, type);
    }

    @Override
    public String toString() {
        return "DetectedForbiddenCommandDTO{" +
                "command='" + command + '\'' +
                ", type=" + type +
                '}';
    }
}