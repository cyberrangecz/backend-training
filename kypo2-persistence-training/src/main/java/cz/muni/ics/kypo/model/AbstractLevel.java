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
import javax.persistence.JoinColumn;
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
  protected Long id;
  @Column(name = "title", nullable = false)
  protected String title;
  @Column(name = "max_score", nullable = false)
  protected int maxScore;
  @Column(name = "level_order", nullable = false)
  protected Long levelOrder;
  @Column(name = "next_level", nullable = false)
  protected Long nextLevel;
  @ManyToOne(fetch = FetchType.LAZY)
  protected TrainingDefinition trainingDefinition;
  @OneToOne(fetch = FetchType.LAZY, optional = true)
  protected PreHook preHook;
  @OneToOne(fetch = FetchType.LAZY, optional = true)
  protected PostHook postHook;

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

  public Long getLevelOrder() {
    return levelOrder;
  }

  public void setLevelOrder(Long levelOrder) {
    this.levelOrder = levelOrder;
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

  public TrainingDefinition getTrainingDefinition() {
    return trainingDefinition;
  }

  public void setTrainingDefinition(TrainingDefinition trainingDefinition) {
    this.trainingDefinition = trainingDefinition;
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
    return "AbstractLevel [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", levelOrder=" + levelOrder + ", nextLevel=" + nextLevel
        + ", trainingDefinition=" + trainingDefinition + ", preHook=" + preHook + ", postHook=" + postHook + ", toString()=" + super.toString() + "]";
  }

}
