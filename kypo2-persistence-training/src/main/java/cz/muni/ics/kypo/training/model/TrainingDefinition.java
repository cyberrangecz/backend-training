package cz.muni.ics.kypo.training.model;

import cz.muni.ics.kypo.training.model.enums.TDState;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * 
 * @author Pavel Seda (441048)
 *
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
	@ManyToMany(fetch = FetchType.LAZY)
	private Set<AuthorRef> authorRef = new HashSet<>();
	@ManyToOne(fetch = FetchType.LAZY)
	private SandboxDefinitionRef sandBoxDefinitionRef;
	@Column(name = "starting_level")
	private Long startingLevel;
	@Column(name = "show_stepper_bar", nullable = false)
	private boolean showStepperBar;

	public TrainingDefinition() {}

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

	public Set<AuthorRef> getAuthorRef() {
		return Collections.unmodifiableSet(authorRef);
	}

	public void setAuthorRef(Set<AuthorRef> authorRef) {
		this.authorRef = authorRef;
	}

	public SandboxDefinitionRef getSandBoxDefinitionRef() {
		return sandBoxDefinitionRef;
	}

	public void setSandBoxDefinitionRef(SandboxDefinitionRef sandBoxDefinitionRef) {
		this.sandBoxDefinitionRef = sandBoxDefinitionRef;
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
		// @formatter:off
    return Objects.equals(description, other.getDescription()) 
        && Arrays.equals(outcomes, other.getOutcomes())
        && Arrays.equals(prerequisities, other.getPrerequisities())
        && Objects.equals(state, other.getState()) 
        && Objects.equals(title, other.getTitle());
    // @formatter:on
	}

	@Override public String toString() {
		return "TrainingDefinition{" + "id=" + id + ", title='" + title + '\'' + ", description='" + description + '\'' + ", prerequisities="
				+ Arrays.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state + ", authorRef=" + authorRef
				+ ", sandBoxDefinitionRef=" + sandBoxDefinitionRef + ", startingLevel=" + startingLevel + ", showStepperBar=" + showStepperBar
				+ '}';
	}
}
