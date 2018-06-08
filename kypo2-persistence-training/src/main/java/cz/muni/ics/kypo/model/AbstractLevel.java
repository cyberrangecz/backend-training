package cz.muni.ics.kypo.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity
@Table(name = "level")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractLevel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "title", nullable = false)
  private String title;
  @Column(name = "max_score", nullable = false)
  private int maxScore;
  @Column(name = "pre_hook", nullable = true)
  private byte[] preHook;
  @Column(name = "post_hook", nullable = true)
  private byte[] postHook;
  @Column(name = "next_level", nullable = false)
  private Long nextLevel;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "abstractLevel")
  private TrainingDefinition trainingDefinition;
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "abstractLevel")
  private Set<TrainingRun> trainingRun = new HashSet<>(0);

  public AbstractLevel() {}

  public AbstractLevel(Long id, String title, int maxScore, byte[] preHook, byte[] postHook, Long nextLevel, TrainingDefinition trainingDefinition,
      Set<TrainingRun> trainingRun) {
    super();
    this.id = id;
    this.title = title;
    this.maxScore = maxScore;
    this.preHook = preHook;
    this.postHook = postHook;
    this.nextLevel = nextLevel;
    this.trainingDefinition = trainingDefinition;
    this.trainingRun = trainingRun;
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

  public byte[] getPreHook() {
    return preHook;
  }

  public void setPreHook(byte[] preHook) {
    this.preHook = preHook;
  }

  public byte[] getPostHook() {
    return postHook;
  }

  public void setPostHook(byte[] postHook) {
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

  public Set<TrainingRun> getTrainingRun() {
    return Collections.unmodifiableSet(trainingRun);
  }

  public void setTrainingRun(Set<TrainingRun> trainingRun) {
    this.trainingRun = trainingRun;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
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
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.getId()))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AbstractLevel [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", preHook=" + preHook + ", postHook=" + postHook + ", nextLevel="
        + nextLevel + ", trainingDefinition=" + trainingDefinition + ", trainingRun=" + trainingRun + "]";
  }

}
