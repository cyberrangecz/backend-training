package cz.muni.ics.kypo.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import cz.muni.ics.kypo.model.enums.TDState;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity
@Table(name = "training_definition")
public class TrainingDefinition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "title", nullable = false)
  private String title;
  @Column(name = "description", nullable = true)
  private String description;
  @Column(name = "prerequisities", nullable = true)
  private String[] prerequisities;
  @Column(name = "outcomes", nullable = true)
  private String[] outcomes;
  @Column(name = "state", nullable = false)
  @Enumerated(EnumType.ORDINAL)
  private TDState state;
  @Column(name = "initial_level", nullable = false)
  private String initialLevel;
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "trainingDefinition")
  private Set<AbstractLevel> abstractLevel = new HashSet<>(0);
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "trainingDefinition")
  private Set<TrainingInstance> trainingInstance = new HashSet<>(0);

  public TrainingDefinition() {}

  public TrainingDefinition(Long id, String title, String description, String[] prerequisities, String[] outcomes, TDState state, String initialLevel,
      Set<AbstractLevel> abstractLevel) {
    super();
    this.id = id;
    this.title = title;
    this.description = description;
    this.prerequisities = prerequisities;
    this.outcomes = outcomes;
    this.state = state;
    this.initialLevel = initialLevel;
    this.abstractLevel = abstractLevel;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String[] getPrerequisities() {
    return prerequisities;
  }

  public void setPrerequisities(String[] prerequisities) {
    this.prerequisities = prerequisities;
  }

  public String[] getOutcomes() {
    return outcomes;
  }

  public void setOutcomes(String[] outcomes) {
    this.outcomes = outcomes;
  }

  public TDState getState() {
    return state;
  }

  public void setState(TDState state) {
    this.state = state;
  }

  public String getInitialLevel() {
    return initialLevel;
  }

  public void setInitialLevel(String initialLevel) {
    this.initialLevel = initialLevel;
  }

  public Set<AbstractLevel> getAbstractLevel() {
    return Collections.unmodifiableSet(abstractLevel);
  }

  public void setAbstractLevel(Set<AbstractLevel> abstractLevel) {
    this.abstractLevel = abstractLevel;
  }

  public Set<TrainingInstance> getTrainingInstance() {
    return Collections.unmodifiableSet(trainingInstance);
  }

  public void setTrainingInstance(Set<TrainingInstance> trainingInstance) {
    this.trainingInstance = trainingInstance;
  }

  @Override
  public String toString() {
    return "TrainingDefinition [id=" + id + ", title=" + title + ", description=" + description + ", prerequisities=" + Arrays.toString(prerequisities)
        + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state + ", initialLevel=" + initialLevel + ", abstractLevel=" + abstractLevel
        + ", trainingInstance=" + trainingInstance + "]";
  }

}
