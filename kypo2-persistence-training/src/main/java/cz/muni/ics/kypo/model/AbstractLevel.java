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
  @Column(name = "level_order", nullable = false)
  private Long levelOrder;
  @Column(name = "next_level", nullable = false)
  private Long nextLevel;
  @ManyToOne(fetch = FetchType.LAZY)
  private TrainingDefinition trainingDefinition;
  @OneToOne(fetch = FetchType.LAZY, optional = true)
  private PreHook preHook;
  @OneToOne(fetch = FetchType.LAZY, optional = true)
  private PostHook postHook;

  public AbstractLevel() {}

  protected Long getId() {
    return id;
  }

  protected void setId(Long id) {
    this.id = id;
  }


  protected String getTitle() {
    return title;
  }

  protected void setTitle(String title) {
    this.title = title;
  }

  protected int getMaxScore() {
    return maxScore;
  }

  protected void setMaxScore(int maxScore) {
    this.maxScore = maxScore;
  }

  protected Long getLevelOrder() {
    return levelOrder;
  }

  protected void setLevelOrder(Long levelOrder) {
    this.levelOrder = levelOrder;
  }

  protected PreHook getPreHook() {
    return preHook;
  }

  protected void setPreHook(PreHook preHook) {
    this.preHook = preHook;
  }

  protected PostHook getPostHook() {
    return postHook;
  }

  protected void setPostHook(PostHook postHook) {
    this.postHook = postHook;
  }

  protected Long getNextLevel() {
    return nextLevel;
  }

  protected void setNextLevel(Long nextLevel) {
    this.nextLevel = nextLevel;
  }

  protected TrainingDefinition getTrainingDefinition() {
    return trainingDefinition;
  }

  protected void setTrainingDefinition(TrainingDefinition trainingDefinition) {
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
