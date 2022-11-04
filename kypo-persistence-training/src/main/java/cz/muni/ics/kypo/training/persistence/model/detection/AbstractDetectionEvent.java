package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.persistence.model.AbstractEntity;
import cz.muni.ics.kypo.training.persistence.model.enums.DetectionEventType;
import cz.muni.ics.kypo.training.persistence.model.enums.CommandType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Class representing Detection event.
 * Detection event occurs based on a submission
 * Detection events can be created based on suspicious
 * submissions in a training instance.
 */
@Entity
@Table(name = "abstract_detection_event")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
        @NamedQuery(
                name = "AbstractDetectionEvent.deleteDetectionEventsOfTrainingInstance",
                query = "DELETE FROM AbstractDetectionEvent de " +
                        "WHERE de.trainingInstanceId = :trainingInstanceId"
        ),
        @NamedQuery(
                name = "AbstractDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT de FROM AbstractDetectionEvent de " +
                        "WHERE de.cheatingDetectionId = :cheatingDetectionId"
        ),
        @NamedQuery(
                name = "AbstractDetectionEvent.deleteDetectionEventsOfCheatingDetection",
                query = "DELETE FROM AbstractDetectionEvent de WHERE de.cheatingDetectionId = :cheatingDetectionId"
        ),
        @NamedQuery(
                name = "AbstractDetectionEvent.getNumberOfDetections",
                query = "SELECT COUNT(de) FROM AbstractDetectionEvent de WHERE de.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class AbstractDetectionEvent extends AbstractEntity<Long> {

    @Column(name = "training_instance_id", nullable = false)
    private Long trainingInstanceId;
    @Column(name = "cheating_detection_id", nullable = false)
    private Long cheatingDetectionId;
    @Column(name = "level_id", nullable = false)
    private Long levelId;
    @Column(name = "level_title", nullable = false)
    private String levelTitle;
    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;
    @Column(name = "participant_count", nullable = false)
    private int participantCount;


    /**
     * Gets training instance id.
     *
     * @return the id of training instance
     */
    public Long getTrainingInstanceId() {
        return trainingInstanceId;
    }

    /**
     * Sets training instance id.
     *
     * @param id the training instance id
     */
    public void setTrainingInstanceId(Long id) {
        this.trainingInstanceId = id;
    }

    /**
     * Gets cheater id.
     *
     * @return the id of cheater
     */
    public Long getCheatingDetectionId() {
        return cheatingDetectionId;
    }

    /**
     * Sets cheater id.
     *
     * @param id the cheater id
     */
    public void setCheatingDetectionId(Long id) {
        this.cheatingDetectionId = id;
    }

    /**
     * Gets level id.
     *
     * @return the id of level
     */
    public Long getLevelId() {
        return levelId;
    }

    /**
     * Sets level id.
     *
     * @param id the level id
     */
    public void setLevelId(Long id) {
        this.levelId = id;
    }

    /**
     * Gets level id.
     *
     * @return the id of level
     */
    public String getLevelTitle() {
        return levelTitle;
    }

    /**
     * Sets level id.
     *
     * @param title the level id
     */
    public void setLevelTitle(String title) {
        this.levelTitle = title;
    }

    /**
     * Gets date.
     *
     * @return the date
     */
    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    /**
     * Sets date.
     *
     * @param detectedAt the date
     */
    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    /**
     * Gets level id.
     *
     * @return the id of level
     */
    public int getParticipantCount() {
        return participantCount;
    }

    /**
     * Sets level id.
     *
     * @param count the level id
     */
    public void setParticipantCount(int count) {
        this.participantCount = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDetectionEvent that = (AbstractDetectionEvent) o;
        return participantCount == that.participantCount &&
                Objects.equals(trainingInstanceId, that.trainingInstanceId) &&
                Objects.equals(cheatingDetectionId, that.cheatingDetectionId) &&
                Objects.equals(levelId, that.levelId) &&
                Objects.equals(levelTitle, that.levelTitle) &&
                Objects.equals(detectedAt, that.detectedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingInstanceId, cheatingDetectionId, levelId, levelTitle, detectedAt, participantCount);
    }

    @Override
    public String toString() {
        return "Cheat{" +
                "trainingInstanceId=" + trainingInstanceId +
                ", cheatingDetectionId=" + cheatingDetectionId +
                ", levelId=" + levelId +
                ", levelTitle=" + levelTitle +
                ", detectedAt=" + detectedAt +
                ", participantCount=" + participantCount + '}';
    }
}
