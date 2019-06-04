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
    @Column(name = "user_ref_login", nullable = false, unique = true)
    private String userRefLogin;
    @Column(name = "user_ref_full_name")
    private String userRefFullName;
    @Column(name = "user_ref_given_name")
    private String userRefGivenName;
    @Column(name = "user_ref_family_name")
    private String userRefFamilyName;
    @ManyToMany(mappedBy = "organizers", fetch = FetchType.LAZY)
    private Set<TrainingInstance> trainingInstances = new HashSet<>();
    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<TrainingDefinition> trainingDefinitions = new HashSet<>();
    @ManyToMany(mappedBy = "organizers", fetch = FetchType.LAZY)
    private Set<BetaTestingGroup> betaTesters = new HashSet<>();

    public UserRef() {
    }

    public UserRef(String userRefLogin) {
        setUserRefLogin(userRefLogin);
    }

    public UserRef(String userRefLogin, String userRefFullName) {
        setUserRefLogin(userRefLogin);
        setUserRefFullName(userRefFullName);
    }

    public UserRef(String userRefLogin, String userRefFullName, String userRefGivenName, String userRefFamilyName) {
        this(userRefLogin, userRefFullName);
        setUserRefGivenName(userRefGivenName);
        setUserRefFamilyName(userRefFamilyName);
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

    public String getUserRefFullName() {
        return userRefFullName;
    }

    public void setUserRefFullName(String userRefFullName) {
        this.userRefFullName = userRefFullName;
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

    public void addViewGroup(BetaTestingGroup viewGroup) {
        this.betaTesters.add(viewGroup);
    }

    public void removeViewGroup(BetaTestingGroup viewGroup) {
        this.betaTesters.remove(viewGroup);
    }

    public void addTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstances.add(trainingInstance);
    }

    public void removeTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstances.remove(trainingInstance);
    }

    public String getUserRefGivenName() {
        return userRefGivenName;
    }

    public void setUserRefGivenName(String userRefGivenName) {
        this.userRefGivenName = userRefGivenName;
    }

    public String getUserRefFamilyName() {
        return userRefFamilyName;
    }

    public void setUserRefFamilyName(String userRefFamilyName) {
        this.userRefFamilyName = userRefFamilyName;
    }

    public Set<BetaTestingGroup> getBetaTesters() {
        return Collections.unmodifiableSet(betaTesters);
    }

    public void setBetaTesters(Set<BetaTestingGroup> betaTesters) {
        this.betaTesters = betaTesters;
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
                ", userRefFullName='" + userRefFullName + '\'' +
                ", userRefGivenName='" + userRefGivenName + '\'' +
                ", userRefFamilyName='" + userRefFamilyName + '\'' +
                '}';
    }
}
