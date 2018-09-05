package cz.muni.ics.kypo.training.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.*;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity(name = "TrainingInstance")
@Table(name = "training_instance")
public class TrainingInstance implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, insertable = false)
  private Long id;
  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;
  @Column(name = "end_time", nullable = true)
  private LocalDateTime endTime;
  @Column(name = "title", nullable = false)
  private String title;
  @Column(name = "pool_size", nullable = false)
  private int poolSize;
  @OneToOne(fetch = FetchType.LAZY)
  private Keyword keyword;
  @ManyToOne(fetch = FetchType.LAZY)
  private TrainingDefinition trainingDefinition;
  @ManyToMany(fetch = FetchType.LAZY)
  private Set<UserRef> organizers = new HashSet<>();
  @ManyToMany(fetch = FetchType.LAZY)
  private Set<SandboxInstanceRef> sandboxInstanceRef = new HashSet<>();

  public TrainingInstance() {}

  public TrainingInstance(Long id, LocalDateTime startTime, LocalDateTime endTime, String title, int poolSize, Keyword keyword,
      TrainingDefinition trainingDefinition, Set<UserRef> organizers, Set<SandboxInstanceRef> sandboxInstanceRef) {
    super();
    this.id = id;
    this.startTime = startTime;
    this.endTime = endTime;
    this.title = title;
    this.poolSize = poolSize;
    this.keyword = keyword;
    this.trainingDefinition = trainingDefinition;
    this.organizers = organizers;
    this.sandboxInstanceRef = sandboxInstanceRef;
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

  public Keyword getKeyword() {
    return keyword;
  }

  public void setKeyword(Keyword keyword) {
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

  public Keyword getKeywords() {
    return keyword;
  }

  public void setKeywords(Keyword keywords) {
    this.keyword = keywords;
  }

  public TrainingDefinition getTrainingDefinition() {
    return trainingDefinition;
  }

  public void setTrainingDefinition(TrainingDefinition trainingDefinition) {
    this.trainingDefinition = trainingDefinition;
  }


  public Set<UserRef> getOrganizers() {
    return Collections.unmodifiableSet(organizers);
  }

  public void setOrganizers(Set<UserRef> organizers) {
    this.organizers = organizers;
  }

  public Set<SandboxInstanceRef> getSandboxInstanceRef() {
    return Collections.unmodifiableSet(sandboxInstanceRef);
  }

  public void setSandboxInstanceRef(Set<SandboxInstanceRef> sandboxInstanceRef) {
    this.sandboxInstanceRef = sandboxInstanceRef;
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyword, startTime, endTime, poolSize, title, trainingDefinition);
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
        && Objects.equals(trainingDefinition, other.getTrainingDefinition());
    // @formatter:on
  }

  @Override
  public String toString() {
    return "TrainingInstance [id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", title=" + title + ", poolSize=" + poolSize + ", keyword="
        + keyword + ", trainingDefinition=" + trainingDefinition + ", toString()=" + super.toString() + "]";
  }

}
