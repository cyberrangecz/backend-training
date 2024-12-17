package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Class representing attachments of Training Level
 */
@Entity
@Table(name = "attachment")
public class Attachment extends AbstractEntity<Long> {

    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_level_id")
    private TrainingLevel trainingLevel;

    /**
     * Instantiates a new Attachment.
     */
    public Attachment() {
    }

    /**
     * Instantiates a new Attachment.
     *
     * @param content      the content
     * @param creationTime the creation time
     * @param trainingLevel    the training level
     */
    public Attachment(String content, LocalDateTime creationTime, TrainingLevel trainingLevel) {
        this.content = content;
        this.creationTime = creationTime;
        this.trainingLevel = trainingLevel;
    }

    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets creation time.
     *
     * @return the creation time
     */
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * Sets creation time.
     *
     * @param creationTime the creation time
     */
    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Gets training level.
     *
     * @return the training level
     */
    public TrainingLevel getTrainingLevel() {
        return trainingLevel;
    }

    /**
     * Sets training level.
     *
     * @param trainingLevel the training level
     */
    public void setTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevel = trainingLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, creationTime);
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + super.getId() +
                ", content='" + content + '\'' +
                ", creationTime=" + creationTime +
                '}';
    }
}

