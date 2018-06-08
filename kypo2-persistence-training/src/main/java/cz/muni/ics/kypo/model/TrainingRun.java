package cz.muni.ics.kypo.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cz.muni.ics.kypo.model.enums.TRState;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity
@Table(name = "training_run")
public class TrainingRun {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "date_time", nullable = false)
  private LocalDateTime localDateTime;
  @Column(name = "event_log_reference", nullable = true)
  private String eventLogReference;
  @Column(name = "state", nullable = false)
  @Enumerated(EnumType.ORDINAL)
  private TRState state;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trainingRun")
  private AbstractLevel abstractLevel;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trainingInstance")
  private TrainingInstance trainingInstance;

  public TrainingRun() {}

  public TrainingRun(Long id, LocalDateTime localDateTime, String eventLogReference, TRState state, AbstractLevel abstractLevel,
      TrainingInstance trainingInstance) {
    super();
    this.id = id;
    this.localDateTime = localDateTime;
    this.eventLogReference = eventLogReference;
    this.state = state;
    this.abstractLevel = abstractLevel;
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

  public AbstractLevel getAbstractLevel() {
    return abstractLevel;
  }

  public void setAbstractLevel(AbstractLevel abstractLevel) {
    this.abstractLevel = abstractLevel;
  }

  public TrainingInstance getTrainingInstance() {
    return trainingInstance;
  }

  public void setTrainingInstance(TrainingInstance trainingInstance) {
    this.trainingInstance = trainingInstance;
  }

  @Override
  public String toString() {
    return "TrainingRun [id=" + id + ", localDateTime=" + localDateTime + ", eventLogReference=" + eventLogReference + ", state=" + state + ", abstractLevel="
        + abstractLevel + ", trainingInstance=" + trainingInstance + "]";
  }

}
