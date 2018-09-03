package cz.muni.ics.kypo.training.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity(name = "SandboxDefinitionRef")
@Table(name = "sandbox_definition_ref")
public class SandboxDefinitionRef implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, insertable = false)
  private Long id;
  @Column(name = "sandbox_definition_ref")
  private Long sandboxDefinitionRef;

  public SandboxDefinitionRef() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSandboxDefinitionRef() {
    return sandboxDefinitionRef;
  }

  public void setSandboxDefinitionRef(Long sandboxDefinitionRef) {
    this.sandboxDefinitionRef = sandboxDefinitionRef;
  }

  @Override
  public String toString() {
    return "SandboxDefinitionRef [id=" + id + ", sandboxDefinitionRef=" + sandboxDefinitionRef + "]";
  }

}
