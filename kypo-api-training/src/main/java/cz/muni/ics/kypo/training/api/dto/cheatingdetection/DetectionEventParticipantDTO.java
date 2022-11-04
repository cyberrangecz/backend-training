package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encapsulates information about detection event participant.
 */
@ApiModel(value = "DetectionEventParticipantDTO", description = "Basic Information about a detection event participant.")
public class DetectionEventParticipantDTO {

    @ApiModelProperty(value = "Ip address of participant.", example = "1.1.1.1")
    private String ipAddress;
    @ApiModelProperty(value = "Time when the event occurred.", example = "1.1.2022 5:55:23")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime occurredAt;
    @ApiModelProperty(value = "Time in which the level was solved.", example = "20")
    private Long solvedInTime;
    @ApiModelProperty(value = "Name of the participant.", example = "John Doe")
    private String name;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Long getSolvedInTime() {
        return solvedInTime;
    }

    public void setSolvedInTime(Long solvedInTime) {
        this.solvedInTime = solvedInTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectionEventParticipantDTO that = (DetectionEventParticipantDTO) o;
        return Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(occurredAt, that.occurredAt) &&
                Objects.equals(solvedInTime, that.solvedInTime) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, occurredAt, solvedInTime, name);
    }

    @Override
    public String toString() {
        return "DetectionEventParticipantDTO{" +
                "ipAddress='" + ipAddress + '\'' +
                ", occurredAt=" + occurredAt +
                ", solvedInTime=" + solvedInTime +
                ", name='" + name + '\'' +
                '}';
    }
}
