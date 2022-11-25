package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "detection_event_participant")
public class DetectionEventParticipant extends AbstractEntity<Long> {

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;
    @Column(name = "occurred_at", nullable = true)
    private LocalDateTime occurredAt;
    @Column(name = "participant_name", nullable = false)
    private String participantName;
    @Column(name = "solved_in_time", nullable = true)
    private Long solvedInTime;
    @Column(name = "name", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_event_id")
    private AbstractDetectionEvent detectionEvent;

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

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public Long getSolvedInTime() {
        return solvedInTime;
    }

    public void setSolvedInTime(Long solvedInTime) {
        this.solvedInTime = solvedInTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AbstractDetectionEvent getDetectionEvent() {
        return detectionEvent;
    }

    public void setDetectionEvent(AbstractDetectionEvent detectionEvent) {
        this.detectionEvent = detectionEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectionEventParticipant that = (DetectionEventParticipant) o;
        return Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(occurredAt, that.occurredAt) &&
                Objects.equals(participantName, that.participantName) &&
                Objects.equals(solvedInTime, that.solvedInTime) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(detectionEvent, that.detectionEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, occurredAt, participantName, solvedInTime, userId, detectionEvent);
    }

    @Override
    public String toString() {
        return "DetectionEventParticipant{" +
                "ipAddress='" + ipAddress + '\'' +
                ", occurredAt=" + occurredAt +
                ", participantName=" + participantName +
                ", solvedInTime=" + solvedInTime +
                ", userId=" + userId +
                ", detectionEvent=" + detectionEvent +
                '}';
    }
}
