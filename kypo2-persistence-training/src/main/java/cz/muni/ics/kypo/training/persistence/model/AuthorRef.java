package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
  @Column(name = "author_ref_login", nullable = false)
  private String authorRefLogin;
  @ManyToMany(mappedBy = "authorRef", fetch = FetchType.LAZY)
  private Set<TrainingDefinition> trainingDefinition = new HashSet<>();

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

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AuthorRef))
			return false;

		AuthorRef authorRef = (AuthorRef) o;

		return getAuthorRefLogin().equals(authorRef.getAuthorRefLogin());
	}

	@Override public int hashCode() {
		return getAuthorRefLogin().hashCode();
	}

	@Override
	public String toString() {
		return "AuthorRef{" + "id=" + id + ", authorRefLogin='" + authorRefLogin + '\'' + ", trainingDefinition=" + trainingDefinition + '}';
	}
}
