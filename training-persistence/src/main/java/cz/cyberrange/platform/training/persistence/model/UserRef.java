package cz.cyberrange.platform.training.persistence.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Local row standing in for a user of the user-and-group microservice, so that training
 * definitions, instances and beta testing groups can reference that user without duplicating its
 * profile data
 */
@Entity
@Table(name = "user_ref", uniqueConstraints = @UniqueConstraint(columnNames = {"user_ref_id"}))
@NamedQueries({
  // Matches on userRefId: takes and returns the external, cross-service user identifiers.
  @NamedQuery(
      name = "UserRef.findUsers",
      query = "SELECT ur FROM UserRef ur WHERE ur.userRefId IN :userRefId"),
  // Matches on userRefId: takes an external user identifier, not the local primary key.
  @NamedQuery(
      name = "UserRef.findUserByUserRefId",
      query = "SELECT ur FROM UserRef ur WHERE ur.userRefId = :userRefId"),
  // Returns userRefId values: the external identifiers of a training instance's participants.
  @NamedQuery(
      name = "UserRef.findParticipantsRefIdsByTrainingInstanceId",
      query =
          "SELECT pr.userRefId FROM TrainingRun tr "
              + "INNER JOIN tr.participantRef pr "
              + "INNER JOIN tr.trainingInstance ti "
              + "WHERE ti.id = :trainingInstanceId")
})
public class UserRef extends AbstractEntity<Long> {

  /**
   * Identifies the user in the user-and-group microservice, and is the value the REST API, the
   * OpenSearch event documents and every other service speak in. Distinct from the inherited
   * primary key, which numbers the row in this service's own database and never leaves it.
   */
  @Column(name = "user_ref_id", nullable = false)
  private Long userRefId;

  @ManyToMany(mappedBy = "organizers", fetch = FetchType.LAZY)
  private Set<TrainingInstance> trainingInstances = new HashSet<>();

  @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
  private Set<TrainingDefinition> trainingDefinitions = new HashSet<>();

  @ManyToMany(mappedBy = "organizers", fetch = FetchType.LAZY)
  private Set<BetaTestingGroup> betaTesters = new HashSet<>();

  public UserRef() {}

  /**
   * Returns the primary key of this row in the training service database. Never accepted from nor
   * exposed to a caller outside this service; a user arriving over the API is identified by {@link
   * #getUserRefId()} instead.
   */
  public Long getId() {
    return super.getId();
  }

  public void setId(Long id) {
    super.setId(id);
  }

  /**
   * Returns the user-and-group identifier of the referenced user, the identifier used across
   * service boundaries
   */
  public Long getUserRefId() {
    return userRefId;
  }

  public void setUserRefId(Long userRefId) {
    this.userRefId = userRefId;
  }

  /**
   * Gets set of training instances user can access.
   *
   * @return the training instances
   */
  public Set<TrainingInstance> getTrainingInstances() {
    return Collections.unmodifiableSet(trainingInstances);
  }

  /**
   * Sets set of training instances user can access.
   *
   * @param trainingInstances the training instances
   */
  public void setTrainingInstances(Set<TrainingInstance> trainingInstances) {
    this.trainingInstances = trainingInstances;
  }

  /**
   * Gets set of training definitions user can access.
   *
   * @return the training definitions
   */
  public Set<TrainingDefinition> getTrainingDefinitions() {
    return Collections.unmodifiableSet(trainingDefinitions);
  }

  /**
   * Sets set of training definitions user can access.
   *
   * @param trainingDefinitions the training definitions
   */
  public void setTrainingDefinitions(Set<TrainingDefinition> trainingDefinitions) {
    this.trainingDefinitions = trainingDefinitions;
  }

  /**
   * Adds definition to the set of training definitions user can access.
   *
   * @param trainingDefinition the training definition
   */
  public void addTrainingDefinition(TrainingDefinition trainingDefinition) {
    this.trainingDefinitions.add(trainingDefinition);
  }

  /**
   * Removes definition from the set of training definitions user can access.
   *
   * @param trainingDefinition the training definition
   */
  public void removeTrainingDefinition(TrainingDefinition trainingDefinition) {
    this.trainingDefinitions.remove(trainingDefinition);
  }

  /**
   * Adds beta testing group that can be accessed by user.
   *
   * @param viewGroup the view group
   */
  public void addViewGroup(BetaTestingGroup viewGroup) {
    this.betaTesters.add(viewGroup);
  }

  /**
   * Removes beta testing group that can be accessed by user.
   *
   * @param viewGroup the view group
   */
  public void removeViewGroup(BetaTestingGroup viewGroup) {
    this.betaTesters.remove(viewGroup);
  }

  /**
   * Adds instance to the set of training instances user can access.
   *
   * @param trainingInstance the training instance
   */
  public void addTrainingInstance(TrainingInstance trainingInstance) {
    this.trainingInstances.add(trainingInstance);
  }

  /**
   * Removes instance from the set of training instances user can access.
   *
   * @param trainingInstance the training instance
   */
  public void removeTrainingInstance(TrainingInstance trainingInstance) {
    this.trainingInstances.remove(trainingInstance);
  }

  /**
   * Gets set of Beta testing groups user can access.
   *
   * @return the beta testers
   */
  public Set<BetaTestingGroup> getBetaTesters() {
    return Collections.unmodifiableSet(betaTesters);
  }

  /**
   * Sets set of Beta testing groups user can access.
   *
   * @param betaTesters the beta testers
   */
  public void setBetaTesters(Set<BetaTestingGroup> betaTesters) {
    this.betaTesters = betaTesters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserRef)) return false;
    UserRef userRef = (UserRef) o;
    return Objects.equals(getUserRefId(), userRef.getUserRefId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUserRefId());
  }

  @Override
  public String toString() {
    return "UserRef{" + "id=" + super.getId() + ", userRefId=" + userRefId + '}';
  }
}
