package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Objects;

/**
 * Class representing hints associated with game level that can be displayed by trainee if they are in need of help with
 * solving given level
 *
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
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "hint_penalty", nullable = false)
    private Integer hintPenalty;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_level_id")
    private GameLevel gameLevel;

    /**
     * Instantiates a new Hint
     */
    public Hint() {
    }

    /**
     * Instantiates a new Hint
     *
     * @param title       title of the hint
     * @param content     text advice of the hint
     * @param hintPenalty score penalty trainee pays to display hint
     * @param gameLevel   game level associated with given hint
     */
    public Hint(String title, String content, Integer hintPenalty, GameLevel gameLevel) {
        this.title = title;
        this.content = content;
        this.hintPenalty = hintPenalty;
        this.gameLevel = gameLevel;
    }

    /**
     * Gets unique identification number of hint
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets unique identification number of hint
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets title of the hint
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title of the hint
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets text advice of the hint
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets text advice of the hint
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets score penalty trainee pays to display hint
     *
     * @return the hint penalty
     */
    public Integer getHintPenalty() {
        return hintPenalty;
    }

    /**
     * Sets score penalty trainee pays to display hint
     *
     * @param hintPenalty the hint penalty
     */
    public void setHintPenalty(Integer hintPenalty) {
        this.hintPenalty = hintPenalty;
    }

    /**
     * Gets game level associated with given hint
     *
     * @return the game level
     */
    public GameLevel getGameLevel() {
        return gameLevel;
    }

    /**
     * Sets game level associated with given hint
     *
     * @param gameLevel the game level
     */
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
