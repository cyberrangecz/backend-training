package cz.muni.ics.kypo.training.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity(name = "UserRef")
@Table(name = "user_ref")
public class UserRef implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, insertable = false)
  private Long id;
  @Column(name = "user_ref_id")
  private Long userRefId;
	@ManyToMany(mappedBy = "organizers", fetch = FetchType.LAZY)
  private Set<TrainingInstance> trainingInstance = new HashSet<>();

  public UserRef() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserRefId() {
    return userRefId;
  }

  public void setUserRefId(Long userRefId) {
    this.userRefId = userRefId;
  }

  public Set<TrainingInstance> getTrainingInstance() {
    return Collections.unmodifiableSet(trainingInstance);
  }

  public void setTrainingInstance(Set<TrainingInstance> trainingInstance) {
    this.trainingInstance = trainingInstance;
  }

  @Override
  public String toString() {
    return "UserRef [id=" + id + ", userRefId=" + userRefId + ", trainingInstance=" + trainingInstance + ", toString()=" + super.toString() + "]";
  }

}
