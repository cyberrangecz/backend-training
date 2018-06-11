package cz.muni.ics.kypo.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

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
@Table(catalog = "training", schema = "public", name = "training_definition")
public class TrainingDefinition implements Serializable {

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
  @Column(name = "state", length = 128, nullable = false)
  @Enumerated(EnumType.STRING)
  private TDState state;
  @OneToMany(fetch = FetchType.LAZY, targetEntity = AbstractLevel.class, mappedBy = "trainingDefinition")
  // private Set<Level> levels = new TreeSet<>(Comparator.comparingLong(AbstractLevel::getId));
  private Set<AbstractLevel> levels = new HashSet<>();
  @OneToMany(fetch = FetchType.LAZY, targetEntity = TrainingInstance.class, mappedBy = "trainingDefinition")
  private Set<TrainingInstance> trainingInstance = new HashSet<>();

  public TrainingDefinition() {}

  public TrainingDefinition(Long id, String title, String description, String[] prerequisities, String[] outcomes, TDState state, Set<AbstractLevel> levels,
      Set<TrainingInstance> trainingInstance) {
    super();
    this.id = id;
    this.title = title;
    this.description = description;
    this.prerequisities = prerequisities;
    this.outcomes = outcomes;
    this.state = state;
    this.levels = levels;
    this.trainingInstance = trainingInstance;
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

  public Set<AbstractLevel> getLevels() {
    return Collections.unmodifiableSet(levels);
  }

  public void setLevels(Set<AbstractLevel> levels) {
    this.levels = levels;
  }

  public Set<TrainingInstance> getTrainingInstance() {
    return Collections.unmodifiableSet(trainingInstance);
  }

  public void setTrainingInstance(Set<TrainingInstance> trainingInstance) {
    this.trainingInstance = trainingInstance;
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, levels, outcomes, prerequisities, state, title, trainingInstance);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TrainingDefinition))
      return false;
    TrainingDefinition other = (TrainingDefinition) obj;
    // @formatter:off
    return Objects.equals(description, other.getDescription()) 
        && Objects.equals(levels, other.getLevels())
        && Arrays.equals(outcomes, other.getOutcomes())
        && Arrays.equals(prerequisities, other.getPrerequisities())
        && Objects.equals(state, other.getState()) 
        && Objects.equals(title, other.getTitle())
        && Objects.equals(trainingInstance, other.getTrainingInstance());
    // @formatter:on
  }

  @Override
  public String toString() {
    return "TrainingDefinition [id=" + id + ", title=" + title + ", description=" + description + ", prerequisities=" + Arrays.toString(prerequisities)
        + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state + ", levels=" + levels + ", trainingInstance=" + trainingInstance + ", toString()="
        + super.toString() + "]";
  }

}
