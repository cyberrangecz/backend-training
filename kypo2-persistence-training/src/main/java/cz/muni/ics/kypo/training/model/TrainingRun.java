package cz.muni.ics.kypo.training.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cz.muni.ics.kypo.training.model.enums.TRState;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity(name = "TrainingRun")
@Table(name = "training_run")
public class TrainingRun implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, insertable = false)
  private Long id;
  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;
  @Column(name = "end_time", nullable = false)
  private LocalDateTime endTime;
  @Column(name = "event_log_reference", nullable = true)
  private String eventLogReference;
  @Column(name = "state", length = 128, nullable = false)
  @Enumerated(EnumType.STRING)
  private TRState state;
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  private AbstractLevel currentLevel;
  @ManyToOne(fetch = FetchType.LAZY)
  private TrainingInstance trainingInstance;
  @ManyToOne(fetch = FetchType.LAZY)
  private SandboxInstanceRef sandboxInstanceRef;

  public TrainingRun() {}


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

  public String getEventLogReference() {
    return eventLogReference;
  }

  public void setEventLogReference(String eventLogReference) {
    this.eventLogReference = eventLogReference;
  }

  public TRState getState() {
    return state;
  }

  public void setState(TRState state) {
    this.state = state;
  }

  public AbstractLevel getCurrentLevel() {
    return currentLevel;
  }

  public void setCurrentLevel(AbstractLevel currentLevel) {
    this.currentLevel = currentLevel;
  }

  public TrainingInstance getTrainingInstance() {
    return trainingInstance;
  }

  public void setTrainingInstance(TrainingInstance trainingInstance) {
    this.trainingInstance = trainingInstance;
  }

  public SandboxInstanceRef getSandboxInstanceRef() {
    return sandboxInstanceRef;
  }


  public void setSandboxInstanceRef(SandboxInstanceRef sandboxInstanceRef) {
    this.sandboxInstanceRef = sandboxInstanceRef;
  }

  @Override
  public int hashCode() {
    return Objects.hash(currentLevel, eventLogReference, startTime, endTime, state, trainingInstance);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TrainingRun))
      return false;
    TrainingRun other = (TrainingRun) obj;
    // @formatter:off
    return Objects.equals(currentLevel, other.getCurrentLevel()) 
        && Objects.equals(eventLogReference, other.getEventLogReference())
        && Objects.equals(startTime, other.getStartTime())
        && Objects.equals(endTime, other.getEndTime()) 
        && Objects.equals(state, other.getState())
        && Objects.equals(trainingInstance, other.getTrainingInstance());
    // @formatter:on
  }

  @Override
  public String toString() {
    return "TrainingRun [id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", eventLogReference=" + eventLogReference + ", state=" + state
        + ", currentLevel=" + currentLevel + ", trainingInstance=" + trainingInstance + ", getClass()=" + getClass() + ", toString()=" + super.toString() + "]";
  }

}
