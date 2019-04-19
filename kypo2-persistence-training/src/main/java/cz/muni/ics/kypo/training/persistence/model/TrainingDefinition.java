package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;

import cz.muni.ics.kypo.training.persistence.model.enums.TDState;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Pavel Seda (441048)
 */
@Entity(name = "TrainingDefinition")
@Table(name = "training_definition")
public class TrainingDefinition implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description", nullable = true)
    private String description;
    @Column(name = "prerequisities", nullable = true)
    private String[] prerequisities;
    @Column(name = "outcomes", nullable = true)
    private String[] outcomes;
    @Column(name = "state", length = 128, nullable = false)
    @Enumerated(EnumType.STRING)
    private TDState state;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "training_definition_user_ref",
            joinColumns = @JoinColumn(name = "training_definition_id"),
            inverseJoinColumns = @JoinColumn(name = "user_ref_id")
    )
    private Set<UserRef> authors = new HashSet<>();
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "beta_testing_group_id", unique = true)
    private BetaTestingGroup betaTestingGroup;
    @Column(name = "sandbox_definition_ref_id", nullable = false)
    private Long sandboxDefinitionRefId;
    @Column(name = "starting_level")
    private Long startingLevel;
    @Column(name = "show_stepper_bar", nullable = false)
    private boolean showStepperBar;
    @Column(name = "last_edited", nullable = false)
    private LocalDateTime lastEdited;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getPrerequisities() {
        return prerequisities;
    }

    public void setPrerequisities(String[] prerequisities) {
        this.prerequisities = prerequisities;
    }

    public String[] getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(String[] outcomes) {
        this.outcomes = outcomes;
    }

    public TDState getState() {
        return state;
    }

    public void setState(TDState state) {
        this.state = state;
    }

    public Set<UserRef> getAuthors() {
        return Collections.unmodifiableSet(authors);
    }

    public void setAuthors(Set<UserRef> authors) {
        this.authors = authors;
    }

    public void addAuthor(UserRef authorRef) {
        this.authors.add(authorRef);
        authorRef.addTrainingDefinition(this);
    }

    public void removeAuthor(UserRef authorRef) {
        this.authors.remove(authorRef);
        authorRef.removeTrainingDefinition(this);
    }

    public BetaTestingGroup getBetaTestingGroup() {
        return betaTestingGroup;
    }

    public void setBetaTestingGroup(BetaTestingGroup betaTestingGroup) {
        this.betaTestingGroup = betaTestingGroup;
    }

    public Long getSandboxDefinitionRefId() {
        return sandboxDefinitionRefId;
    }

    public void setSandboxDefinitionRefId(Long sandboxDefinitionRefId) {
        this.sandboxDefinitionRefId = sandboxDefinitionRefId;
    }

    public Long getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(Long startingLevel) {
        this.startingLevel = startingLevel;
    }

    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

    public LocalDateTime getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(LocalDateTime lastEdited) {
        this.lastEdited = lastEdited;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, outcomes, prerequisities, state, title);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof TrainingDefinition))
            return false;
        TrainingDefinition other = (TrainingDefinition) obj;
        return Objects.equals(description, other.getDescription())
                && Arrays.equals(outcomes, other.getOutcomes())
                && Arrays.equals(prerequisities, other.getPrerequisities())
                && Objects.equals(state, other.getState())
                && Objects.equals(title, other.getTitle());
    }

    @Override
    public String toString() {
        return "TrainingDefinition{" + "id=" + id + ", title='" + title + '\'' + ", description='" + description + '\''
                + ", prerequisities=" + Arrays.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state
                + ", authors=" + authors + ", betaTestingGroup=" + betaTestingGroup + ", sandboxDefinitionRefId=" + sandboxDefinitionRefId
                + ", startingLevel=" + startingLevel + ", showStepperBar=" + showStepperBar + ", lastEdited=" + lastEdited + '}';
    }
}
