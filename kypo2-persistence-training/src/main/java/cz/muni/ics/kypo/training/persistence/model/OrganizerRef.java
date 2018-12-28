package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Seda (441048)
 */
@Entity(name = "OrganizerRef")
@Table(name = "organizer_ref")
public class OrganizerRef implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "organizer_ref_login")
    private String organizersRefLogin;
    @ManyToMany(mappedBy = "organizers", fetch = FetchType.LAZY)
    private Set<TrainingInstance> trainingInstance = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganizersRefLogin() {
        return organizersRefLogin;
    }

    public void setOrganizersRefLogin(String organizersRefLogin) {
        this.organizersRefLogin = organizersRefLogin;
    }

    public Set<TrainingInstance> getTrainingInstance() {
        return Collections.unmodifiableSet(trainingInstance);
    }

    public void setTrainingInstance(Set<TrainingInstance> trainingInstance) {
        this.trainingInstance = trainingInstance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganizerRef)) return false;
        OrganizerRef organizerRef = (OrganizerRef) o;
        return Objects.equals(getOrganizersRefLogin(), organizerRef.getOrganizersRefLogin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrganizersRefLogin());
    }

    @Override
    public String toString() {
        return "OrganizerRef{" +
                "id=" + id +
                ", organizersRefLogin='" + organizersRefLogin + '\'' +
                ", trainingInstance=" + trainingInstance +
                '}';
    }
}
