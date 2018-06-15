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
  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;
  @Column(name = "end_time", nullable = true)
  private LocalDateTime endTime;
  @Column(name = "title", nullable = false)
  private String title;
  @Column(name = "pool_size", nullable = false)
  private int poolSize;
  @Column(name = "\"keyword\"", nullable = false)
  private String keyword;
  @ManyToOne(fetch = FetchType.LAZY)
  private TrainingDefinition trainingDefinition;
  @OneToMany(fetch = FetchType.LAZY, targetEntity = TrainingRun.class, mappedBy = "trainingInstance")
  private Set<TrainingRun> trainingRun = new HashSet<>();

  public TrainingInstance() {}

  public TrainingInstance(Long id, LocalDateTime startTime, LocalDateTime endTime, String title, int poolSize, String keyword,
      TrainingDefinition trainingDefinition, Set<TrainingRun> trainingRun) {
    super();
    this.id = id;
    this.startTime = startTime;
    this.endTime = endTime;
    this.title = title;
    this.poolSize = poolSize;
    this.keyword = keyword;
    this.trainingDefinition = trainingDefinition;
    this.trainingRun = trainingRun;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
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
    return keyword;
  }

  public void setKeywords(String keywords) {
    this.keyword = keywords;
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
    return Objects.hash(keyword, startTime, endTime, poolSize, title, trainingDefinition, trainingRun);
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
    return Objects.equals(keyword, other.getKeywords()) 
        && Objects.equals(startTime, other.getStartTime())
        && Objects.equals(endTime, other.getEndTime())
        && Objects.equals(poolSize, other.getPoolSize())
        && Objects.equals(title, other.getTitle())
        && Objects.equals(trainingDefinition, other.getTrainingDefinition()) 
        && Objects.equals(trainingRun, other.getTrainingRun());
    // @formatter:on
  }

  @Override
  public String toString() {
    return "TrainingInstance [id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", title=" + title + ", poolSize=" + poolSize + ", keyword="
        + keyword + ", trainingDefinition=" + trainingDefinition + ", trainingRun=" + trainingRun + ", toString()=" + super.toString() + "]";
  }

}
