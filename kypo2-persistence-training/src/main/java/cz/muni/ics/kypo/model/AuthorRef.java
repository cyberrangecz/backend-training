package cz.muni.ics.kypo.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity(name = "AuthorRef")
@Table(name = "author_ref")
public class AuthorRef implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  @Column(name = "author_ref_id")
  private Long authorRefId;
  @ManyToMany(mappedBy = "authorRef", fetch = FetchType.LAZY)
  private Set<TrainingDefinition> trainingDefinition = new HashSet<>();

  public AuthorRef() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getAuthorRefId() {
    return authorRefId;
  }

  public void setAuthorRefId(Long authorRefId) {
    this.authorRefId = authorRefId;
  }

  public Set<TrainingDefinition> getTrainingDefinition() {
    return Collections.unmodifiableSet(trainingDefinition);
  }

  public void setTrainingDefinition(Set<TrainingDefinition> trainingDefinition) {
    this.trainingDefinition = trainingDefinition;
  }
  
  

  @Override
  public String toString() {
    return "AuthorRef [id=" + id + ", authorRefId=" + authorRefId + ", trainingDefinition=" + trainingDefinition + ", toString()=" + super.toString() + "]";
  }

}
