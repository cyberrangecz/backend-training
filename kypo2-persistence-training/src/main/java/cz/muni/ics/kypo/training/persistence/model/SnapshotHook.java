package cz.muni.ics.kypo.training.persistence.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Pavel Seda (441048)
 */
@Entity(name = "SnapshotHook")
@Table(name = "snapshot_hook")
public class SnapshotHook implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "snapshot", nullable = false)
    private String snapshot;
    @OneToOne(mappedBy = "snapshotHook", fetch = FetchType.LAZY)
    private AbstractLevel abstractLevel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AbstractLevel getAbstractLevel() {
        return abstractLevel;
    }

    public void setAbstractLevel(AbstractLevel abstractLevel) {
        this.abstractLevel = abstractLevel;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public String toString() {
        return "SnapshotHook{" +
                "id=" + id +
                ", snapshot=" + snapshot +
                '}';
    }
}
