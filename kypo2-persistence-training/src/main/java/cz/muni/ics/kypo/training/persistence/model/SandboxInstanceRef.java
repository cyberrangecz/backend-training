package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;

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
	@Column(name = "id", unique = true, nullable = false, insertable = false)
	private Long id;
	@Column(name = "sandbox_instance_ref")
	private Long sandboxInstanceRefId;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "training_instance_id", nullable = false)
	private TrainingInstance trainingInstance;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSandboxInstanceRef() {
		return sandboxInstanceRefId;
	}

	public void setSandboxInstanceRef(Long sandboxInstanceRef) {
		this.sandboxInstanceRefId = sandboxInstanceRef;
	}

	public TrainingInstance getTrainingInstance() {
		return trainingInstance;
	}

	public void setTrainingInstance(TrainingInstance trainingInstance) {
		this.trainingInstance = trainingInstance;
	}

	@Override
	public String toString() {
		return "SandboxInstanceRef [id=" + id + ", sandboxInstanceRef=" + sandboxInstanceRefId + ", trainingInstance=" + trainingInstance
				+ ", toString()=" + super.toString() + "]";
	}

}
