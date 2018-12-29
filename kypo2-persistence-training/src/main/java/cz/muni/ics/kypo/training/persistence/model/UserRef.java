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
@Table(name = "user_ref")
public class UserRef implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "user_ref_login", unique = true)
    private String userRefLogin;
    @ManyToMany(mappedBy = "organizers", fetch = FetchType.LAZY)
    private Set<TrainingInstance> trainingInstances = new HashSet<>();
    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<TrainingDefinition> trainingDefinitions = new HashSet<>();
    @ManyToMany(mappedBy = "organizers", fetch = FetchType.LAZY)
    private Set<TDViewGroup> tdViewGroups = new HashSet<>();

    public UserRef() {
    }

    public UserRef(String userRefLogin) {
        this.userRefLogin = userRefLogin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserRefLogin() {
        return userRefLogin;
    }

    public void setUserRefLogin(String userRefLogin) {
        this.userRefLogin = userRefLogin;
    }

    public Set<TrainingInstance> getTrainingInstances() {
        return Collections.unmodifiableSet(trainingInstances);
    }

    public void setTrainingInstances(Set<TrainingInstance> trainingInstances) {
        this.trainingInstances = trainingInstances;
    }

    public Set<TrainingDefinition> getTrainingDefinitions() {
        return Collections.unmodifiableSet(trainingDefinitions);
    }

    public void setTrainingDefinitions(Set<TrainingDefinition> trainingDefinitions) {
        this.trainingDefinitions = trainingDefinitions;
    }

    public void addTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinitions.add(trainingDefinition);
    }

    public void removeTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinitions.remove(trainingDefinition);
    }

    public Set<TDViewGroup> getTdViewGroups() {
        return Collections.unmodifiableSet(tdViewGroups);
    }

    public void setTdViewGroups(Set<TDViewGroup> tdViewGroups) {
        this.tdViewGroups = tdViewGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRef)) return false;
        UserRef userRef = (UserRef) o;
        return Objects.equals(getUserRefLogin(), userRef.getUserRefLogin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserRefLogin());
    }

    @Override
    public String toString() {
        return "UserRef{" +
                "id=" + id +
                ", userRefLogin='" + userRefLogin + '\'' +
                ", tdViewGroups=" + tdViewGroups +
                '}';
    }
}
