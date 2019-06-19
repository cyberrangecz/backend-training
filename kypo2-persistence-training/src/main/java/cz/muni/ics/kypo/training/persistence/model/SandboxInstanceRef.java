package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Class representing DB reference for sandbox instance
 *
 * @author Pavel Seda (441048)
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_instance_id", nullable = false)
    private TrainingInstance trainingInstance;

    /**
     * Gets unique identification number of Sandbox instance reference
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets unique identification number of Sandbox instance reference
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets reference number of Sandbox instance
     *
     * @return the sandbox instance ref
     */
    public Long getSandboxInstanceRef() {
        return sandboxInstanceRefId;
    }

    /**
     * Sets reference number of Sandbox instance
     *
     * @param sandboxInstanceRef the sandbox instance ref
     */
    public void setSandboxInstanceRef(Long sandboxInstanceRef) {
        this.sandboxInstanceRefId = sandboxInstanceRef;
    }

    /**
     * Gets associated training instance
     *
     * @return the training instance
     */
    public TrainingInstance getTrainingInstance() {
        return trainingInstance;
    }

    /**
     * Sets associated training instance
     *
     * @param trainingInstance the training instance
     */
    public void setTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstance = trainingInstance;
    }

    @Override
    public String toString() {
        return "SandboxInstanceRef{" +
                "id=" + id +
                ", sandboxInstanceRefId=" + sandboxInstanceRefId +
                '}';
    }
}
