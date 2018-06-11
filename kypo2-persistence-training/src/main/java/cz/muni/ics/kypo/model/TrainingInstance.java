package cz.muni.ics.kypo.model;

import java.io.Serializable;
import java.time.LocalDateTime;
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
@Table(catalog = "training", schema = "public", name = "training_instance")
public class TrainingInstance implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "date_time", nullable = false)
  private LocalDateTime localDateTime;
  @Column(name = "life_time", nullable = false)
  private int lifeTime;
  @Column(name = "title", nullable = false)
  private String title;
  @Column(name = "pool_size", nullable = false)
  private int poolSize;
  @Column(name = "keywords", nullable = false)
  private String keywords;
  @ManyToOne(fetch = FetchType.LAZY)
  private TrainingDefinition trainingDefinition;
  @OneToMany(fetch = FetchType.LAZY, targetEntity = TrainingRun.class, mappedBy = "trainingInstance")
  private Set<TrainingRun> trainingRun = new HashSet<>();

  public TrainingInstance() {}

  public TrainingInstance(Long id, LocalDateTime localDateTime, int lifeTime, String title, int poolSize, String keywords,
      TrainingDefinition trainingDefinition, Set<TrainingRun> trainingRun) {
    super();
    this.id = id;
    this.localDateTime = localDateTime;
    this.lifeTime = lifeTime;
    this.title = title;
    this.poolSize = poolSize;
    this.keywords = keywords;
    this.trainingDefinition = trainingDefinition;
    this.trainingRun = trainingRun;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getLocalDateTime() {
    return localDateTime;
  }

  public void setLocalDateTime(LocalDateTime localDateTime) {
    this.localDateTime = localDateTime;
  }

  public int getLifeTime() {
    return lifeTime;
  }

  public void setLifeTime(int lifeTime) {
    this.lifeTime = lifeTime;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getPoolSize() {
    return poolSize;
  }

  public void setPoolSize(int poolSize) {
    this.poolSize = poolSize;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
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
    return Objects.hash(keywords, lifeTime, localDateTime, poolSize, title, trainingDefinition, trainingRun);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TrainingInstance))
      return false;
    TrainingInstance other = (TrainingInstance) obj;
    // @formatter:off
    return Objects.equals(keywords, other.getKeywords()) 
        && Objects.equals(lifeTime, other.getLifeTime())
        && Objects.equals(localDateTime, other.getLocalDateTime())
        && Objects.equals(poolSize, other.getPoolSize())
        && Objects.equals(title, other.getTitle())
        && Objects.equals(trainingDefinition, other.getTrainingDefinition()) 
        && Objects.equals(trainingRun, other.getTrainingRun());
    // @formatter:on
  }

  @Override
  public String toString() {
    return "TrainingInstance [id=" + id + ", localDateTime=" + localDateTime + ", lifeTime=" + lifeTime + ", title=" + title + ", poolSize=" + poolSize
        + ", keywords=" + keywords + ", trainingDefinition=" + trainingDefinition + ", trainingRun=" + trainingRun + ", toString()=" + super.toString() + "]";
  }

}
