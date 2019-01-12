package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Pavel Seda (441048)
 */
@Entity(name = "Hint")
@Table(name = "hint")
public class Hint implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Lob
    @Type(type = "org.hibernate.type.StringType")
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "hint_penalty", nullable = false)
    private Integer hintPenalty;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_level_id")
    private GameLevel gameLevel;

    public Hint() {
    }

    public Hint(String title, String content, Integer hintPenalty, GameLevel gameLevel) {
        this.title = title;
        this.content = content;
        this.hintPenalty = hintPenalty;
        this.gameLevel = gameLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getHintPenalty() {
        return hintPenalty;
    }

    public void setHintPenalty(Integer hintPenalty) {
        this.hintPenalty = hintPenalty;
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
        if (!(o instanceof Hint)) return false;
        Hint hint = (Hint) o;
        return Objects.equals(getTitle(), hint.getTitle()) &&
                Objects.equals(getContent(), hint.getContent()) &&
                Objects.equals(getHintPenalty(), hint.getHintPenalty());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getContent(), getHintPenalty());
    }

    @Override
    public String toString() {
        return "Hint{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", hintPenalty=" + hintPenalty +
                '}';
    }
}
