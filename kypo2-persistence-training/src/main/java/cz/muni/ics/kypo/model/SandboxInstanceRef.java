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
@Entity(name = "SandboxInstanceRef")
@Table(name = "sandbox_instance_ref")
public class SandboxInstanceRef implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  @Column(name = "sandbox_instance_ref")
  private Long sandboxInstanceRef;
  @ManyToMany(mappedBy = "sandboxInstanceRef", fetch = FetchType.LAZY)
  private Set<TrainingInstance> trainingInstance = new HashSet<>();

  public SandboxInstanceRef() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSandboxInstanceRef() {
    return sandboxInstanceRef;
  }

  public void setSandboxInstanceRef(Long sandboxInstanceRef) {
    this.sandboxInstanceRef = sandboxInstanceRef;
  }

  public Set<TrainingInstance> getTrainingInstance() {
    return Collections.unmodifiableSet(trainingInstance);
  }

  public void setTrainingInstance(Set<TrainingInstance> trainingInstance) {
    this.trainingInstance = trainingInstance;
  }

  @Override
  public String toString() {
    return "SandboxInstanceRef [id=" + id + ", sandboxInstanceRef=" + sandboxInstanceRef + ", trainingInstance=" + trainingInstance + ", toString()="
        + super.toString() + "]";
  }

}
