package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Class representing attachments of Game Level
 */
@Entity
@Table(name = "attachment")
public class Attachment extends AbstractEntity<Long> {

    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_level_id")
    private GameLevel gameLevel;

    public Attachment() {
    }

    public Attachment(String content, LocalDateTime creationTime, GameLevel gameLevel) {
        this.content = content;
        this.creationTime = creationTime;
        this.gameLevel = gameLevel;
    }

    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public GameLevel getGameLevel() {
        return gameLevel;
    }

    public void setGameLevel(GameLevel gameLevel) {
        this.gameLevel = gameLevel;
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

