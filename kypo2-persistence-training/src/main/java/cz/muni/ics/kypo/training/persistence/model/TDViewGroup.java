package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Seda
 */
@Entity
@Table(name = "td_view_group")
public class TDViewGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "td_view_group_user_ref",
            joinColumns = @JoinColumn(name = "td_view_group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_ref_id")
    )
    private Set<UserRef> organizers = new HashSet<>();
    @OneToOne(mappedBy = "tdViewGroup", fetch = FetchType.LAZY)
    private TrainingDefinition trainingDefinition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<UserRef> getOrganizers() {
        return Collections.unmodifiableSet(organizers);
    }

    public void setOrganizers(Set<UserRef> organizers) {
        this.organizers = organizers;
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
        if (!(o instanceof TDViewGroup)) return false;
        TDViewGroup that = (TDViewGroup) o;
        return Objects.equals(getOrganizers(), that.getOrganizers()) &&
                Objects.equals(getTrainingDefinition(), that.getTrainingDefinition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrganizers(), getTrainingDefinition());
    }

    @Override
    public String toString() {
        return "TDViewGroup{" +
                "id=" + id +
                ", organizers=" + organizers +
                ", trainingDefinition=" + trainingDefinition +
                '}';
    }
}
