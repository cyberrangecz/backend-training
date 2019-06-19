package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class representing DB reference for user and training instances and definition they can access
 *
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

    /**
     * Instantiates a new user reference
     */
    public UserRef() {
    }

    /**
     * Instantiates a new user reference
     *
     * @param userRefLogin login of user
     */
    public UserRef(String userRefLogin) {
        setUserRefLogin(userRefLogin);
    }

    /**
     * Instantiates a new user reference
     *
     * @param userRefLogin    login of user
     * @param userRefFullName full name of user
     */
    public UserRef(String userRefLogin, String userRefFullName) {
        setUserRefLogin(userRefLogin);
        setUserRefFullName(userRefFullName);
    }

    /**
     * Instantiates a new user reference
     *
     * @param userRefLogin      login of user
     * @param userRefFullName   full name of user
     * @param userRefGivenName  given name of user
     * @param userRefFamilyName family name of user
     */
    public UserRef(String userRefLogin, String userRefFullName, String userRefGivenName, String userRefFamilyName) {
        this(userRefLogin, userRefFullName);
        setUserRefGivenName(userRefGivenName);
        setUserRefFamilyName(userRefFamilyName);
    }

    /**
     * Gets unique identification number of user reference
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets unique identification number of user reference
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets login of user
     *
     * @return the user ref login
     */
    public String getUserRefLogin() {
        return userRefLogin;
    }

    /**
     * Sets login of user
     *
     * @param userRefLogin the user ref login
     */
    public void setUserRefLogin(String userRefLogin) {
        this.userRefLogin = userRefLogin;
    }

    /**
     * Gets full name of user
     *
     * @return the user ref full name
     */
    public String getUserRefFullName() {
        return userRefFullName;
    }

    /**
     * Sets full name of user
     *
     * @param userRefFullName the user ref full name
     */
    public void setUserRefFullName(String userRefFullName) {
        this.userRefFullName = userRefFullName;
    }

    /**
     * Gets set of training instances user can access
     *
     * @return the training instances
     */
    public Set<TrainingInstance> getTrainingInstances() {
        return Collections.unmodifiableSet(trainingInstances);
    }

    /**
     * Sets set of training instances user can access
     *
     * @param trainingInstances the training instances
     */
    public void setTrainingInstances(Set<TrainingInstance> trainingInstances) {
        this.trainingInstances = trainingInstances;
    }

    /**
     * Gets set of training definitions user can access
     *
     * @return the training definitions
     */
    public Set<TrainingDefinition> getTrainingDefinitions() {
        return Collections.unmodifiableSet(trainingDefinitions);
    }

    /**
     * Sets set of training definitions user can access
     *
     * @param trainingDefinitions the training definitions
     */
    public void setTrainingDefinitions(Set<TrainingDefinition> trainingDefinitions) {
        this.trainingDefinitions = trainingDefinitions;
    }

    /**
     * Adds definition to the set of training definitions user can access
     *
     * @param trainingDefinition the training definition
     */
    public void addTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinitions.add(trainingDefinition);
    }

    /**
     * Removes definition from the set of training definitions user can access
     *
     * @param trainingDefinition the training definition
     */
    public void removeTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinitions.remove(trainingDefinition);
    }

    /**
     * Adds beta testing group that can be accessed by user
     *
     * @param viewGroup the view group
     */
    public void addViewGroup(BetaTestingGroup viewGroup) {
        this.betaTesters.add(viewGroup);
    }

    /**
     * Removes beta testing group that can be accessed by user
     *
     * @param viewGroup the view group
     */
    public void removeViewGroup(BetaTestingGroup viewGroup) {
        this.betaTesters.remove(viewGroup);
    }

    /**
     * Adds instance to the set of training instances user can access
     *
     * @param trainingInstance the training instance
     */
    public void addTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstances.add(trainingInstance);
    }

    /**
     * Removes instance from the set of training instances user can access
     *
     * @param trainingInstance the training instance
     */
    public void removeTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstances.remove(trainingInstance);
    }

    /**
     * Gets given name of user
     *
     * @return the user ref given name
     */
    public String getUserRefGivenName() {
        return userRefGivenName;
    }

    /**
     * Sets given name of user
     *
     * @param userRefGivenName the user ref given name
     */
    public void setUserRefGivenName(String userRefGivenName) {
        this.userRefGivenName = userRefGivenName;
    }

    /**
     * Gets family name of user
     *
     * @return the user ref family name
     */
    public String getUserRefFamilyName() {
        return userRefFamilyName;
    }

    /**
     * Sets family name of user
     *
     * @param userRefFamilyName the user ref family name
     */
    public void setUserRefFamilyName(String userRefFamilyName) {
        this.userRefFamilyName = userRefFamilyName;
    }

    /**
     * Gets set of Beta testing groups user can access
     *
     * @return the beta testers
     */
    public Set<BetaTestingGroup> getBetaTesters() {
        return Collections.unmodifiableSet(betaTesters);
    }

    /**
     * Sets set of Beta testing groups user can access
     *
     * @param betaTesters the beta testers
     */
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
