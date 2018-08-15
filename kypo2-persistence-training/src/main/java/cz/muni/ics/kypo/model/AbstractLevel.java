package cz.muni.ics.kypo.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
  @OneToOne(fetch = FetchType.LAZY, optional = true)
  private PreHook preHook;
  @OneToOne(fetch = FetchType.LAZY, optional = true)
  private PostHook postHook;

  public AbstractLevel() {}

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
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof AbstractLevel))
      return false;
    AbstractLevel other = (AbstractLevel) obj;
    return Objects.equals(id, other.getId());
  }

  @Override
  public String toString() {
    return "AbstractLevel [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", nextLevel=" + nextLevel
        + ", preHook=" + preHook + ", postHook=" + postHook + ", toString()=" + super.toString() + "]";
  }

}
