package cz.muni.ics.kypo.training.model;

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
  @Column(name = "id", unique = true, nullable = false, insertable = false)
  private Long id;
  @Column(name = "author_ref_id")
  private String authorRefLogin;
  @ManyToMany(mappedBy = "authorRef", fetch = FetchType.LAZY)
  private Set<TrainingDefinition> trainingDefinition = new HashSet<>();

  public AuthorRef() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAuthorRefLogin() {
    return authorRefLogin;
  }

  public void setAuthorRefLogin(String authorRefLogin) {
    this.authorRefLogin = authorRefLogin;
  }

  public Set<TrainingDefinition> getTrainingDefinition() {
    return trainingDefinition;
  }

  public void setTrainingDefinition(Set<TrainingDefinition> trainingDefinition) {
    this.trainingDefinition = trainingDefinition;
  }

  @Override
  public String toString() {
    return "AuthorRef{" +
            "id=" + id +
            ", authorRefLogin='" + authorRefLogin + '\'' +
            ", trainingDefinition=" + trainingDefinition +
            '}';
  }
}
