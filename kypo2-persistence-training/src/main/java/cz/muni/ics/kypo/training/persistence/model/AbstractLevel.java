package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Pavel Seda
 */
@Entity(name = "AbstractLevel")
@Table(name = "abstract_level")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractLevel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "max_score", nullable = false)
    private int maxScore;
    @OneToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL)
    private SnapshotHook snapshotHook;
    @Column(name = "estimated_duration")
    private long estimatedDuration;
    @Column(name = "order_in_training_definition", nullable = false)
    private int order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_definition_id")
    private TrainingDefinition trainingDefinition;

    public AbstractLevel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public SnapshotHook getSnapshotHook() {
        return snapshotHook;
    }

    public void setSnapshotHook(SnapshotHook snapshotHook) {
        this.snapshotHook = snapshotHook;
    }

    public long getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public TrainingDefinition getTrainingDefinition() {
        return trainingDefinition;
    }

    public void setTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractLevel)) return false;
        AbstractLevel that = (AbstractLevel) o;
        return getMaxScore() == that.getMaxScore() &&
                Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMaxScore());
    }
}
