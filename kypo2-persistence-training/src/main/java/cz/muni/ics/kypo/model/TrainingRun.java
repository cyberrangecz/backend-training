package cz.muni.ics.kypo.model;

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

import cz.muni.ics.kypo.model.enums.TRState;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity
@Table(catalog = "training", schema = "public", name = "training_run")
public class TrainingRun implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "date_time", nullable = false)
  private LocalDateTime localDateTime;
  @Column(name = "event_log_reference", nullable = true)
  private String eventLogReference;
  @Column(name = "state", length = 128, nullable = false)
  @Enumerated(EnumType.STRING)
  private TRState state;
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  private AbstractLevel level;
  @ManyToOne(fetch = FetchType.EAGER)
  private TrainingInstance trainingInstance;

  public TrainingRun() {}

  public TrainingRun(Long id, LocalDateTime localDateTime, String eventLogReference, TRState state, AbstractLevel abstractLevel,
      TrainingInstance trainingInstance) {
    super();
    this.id = id;
    this.localDateTime = localDateTime;
    this.eventLogReference = eventLogReference;
    this.state = state;
    this.level = abstractLevel;
    this.trainingInstance = trainingInstance;
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

  public AbstractLevel getLevel() {
    return level;
  }

  public void setLevel(AbstractLevel level) {
    this.level = level;
  }

  public TrainingInstance getTrainingInstance() {
    return trainingInstance;
  }

  public void setTrainingInstance(TrainingInstance trainingInstance) {
    this.trainingInstance = trainingInstance;
  }

  @Override
  public int hashCode() {
    return Objects.hash(level, eventLogReference, localDateTime, state, trainingInstance);
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
    return Objects.equals(level, other.getLevel()) 
        && Objects.equals(eventLogReference, other.getEventLogReference())
        && Objects.equals(localDateTime, other.getLocalDateTime()) 
        && Objects.equals(state, other.getState())
        && Objects.equals(trainingInstance, other.getTrainingInstance());
    // @formatter:on
  }

  @Override
  public String toString() {
    return "TrainingRun [id=" + id + ", localDateTime=" + localDateTime + ", eventLogReference=" + eventLogReference + ", state=" + state + ", abstractLevel="
        + level + ", trainingInstance=" + trainingInstance + ", toString()=" + super.toString() + "]";
  }

}
