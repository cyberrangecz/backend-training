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
@Table(name = "beta_testing_group")
public class BetaTestingGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "beta_testing_group_user_ref",
            joinColumns = @JoinColumn(name = "beta_testing_group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_ref_id")
    )
    private Set<UserRef> organizers = new HashSet<>();
    @OneToOne(mappedBy = "betaTestingGroup", fetch = FetchType.LAZY)
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

    public void addOrganizer(UserRef organizer) {
        this.organizers.add(organizer);
        organizer.addViewGroup(this);
    }

    public void removeOrganizer(UserRef organizer) {
        this.organizers.remove(organizer);
        organizer.removeViewGroup(this);
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
        if (!(o instanceof BetaTestingGroup)) return false;
        BetaTestingGroup that = (BetaTestingGroup) o;
        return Objects.equals(getOrganizers(), that.getOrganizers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrganizers());
    }

    @Override
    public String toString() {
        return "BetaTestingGroup{" +
                "id=" + id +
                ", organizers=" + organizers +
                '}';
    }
}
