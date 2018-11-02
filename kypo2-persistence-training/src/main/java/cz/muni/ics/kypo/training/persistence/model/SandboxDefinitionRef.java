package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;

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
	private Long sandboxDefinitionRefId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSandboxDefinitionRef() {
		return sandboxDefinitionRefId;
	}

	public void setSandboxDefinitionRef(Long sandboxDefinitionRef) {
		this.sandboxDefinitionRefId = sandboxDefinitionRef;
	}

	@Override
	public String toString() {
		return "SandboxDefinitionRef [id=" + id + ", sandboxDefinitionRef=" + sandboxDefinitionRefId + "]";
	}

}
