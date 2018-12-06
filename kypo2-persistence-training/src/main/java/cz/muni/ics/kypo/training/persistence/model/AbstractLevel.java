package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Pavel Seda
 */
@Entity(name = "AbstractLevel")
@Table(name = "abstract_level")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractLevel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "max_score", nullable = false)
    private int maxScore;
    @Column(name = "next_level")
    private Long nextLevel;
    @OneToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL)
    private PreHook preHook;
    @OneToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL)
    private PostHook postHook;

    public AbstractLevel() {
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

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public PreHook getPreHook() {
        return preHook;
    }

    public void setPreHook(PreHook preHook) {
        this.preHook = preHook;
    }

    public PostHook getPostHook() {
        return postHook;
    }

    public void setPostHook(PostHook postHook) {
        this.postHook = postHook;
    }

    public Long getNextLevel() {
        return nextLevel;
    }

    public void setNextLevel(Long nextLevel) {
        this.nextLevel = nextLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractLevel)) return false;

        AbstractLevel that = (AbstractLevel) o;

        if (getMaxScore() != that.getMaxScore()) return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null) return false;
        if (getNextLevel() != null ? !getNextLevel().equals(that.getNextLevel()) : that.getNextLevel() != null)
            return false;
        if (getPreHook() != null ? !getPreHook().equals(that.getPreHook()) : that.getPreHook() != null) return false;
        return getPostHook() != null ? getPostHook().equals(that.getPostHook()) : that.getPostHook() == null;
    }

    @Override
    public String toString() {
        return "AbstractLevel [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", nextLevel=" + nextLevel + ", preHook=" + preHook
                + ", postHook=" + postHook + ", toString()=" + super.toString() + "]";
    }

}
