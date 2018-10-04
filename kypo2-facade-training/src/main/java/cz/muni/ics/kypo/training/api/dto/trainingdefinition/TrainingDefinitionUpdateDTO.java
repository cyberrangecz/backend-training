package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import cz.muni.ics.kypo.training.api.dto.AuthorRefDTO;
import cz.muni.ics.kypo.training.api.dto.SandboxDefinitionRefDTO;
import cz.muni.ics.kypo.training.model.enums.TDState;
import io.swagger.annotations.ApiModel;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiModel(value = "TrainingDefinitionUpdateDTO", description = "Training Definition to update.")
public class TrainingDefinitionUpdateDTO {

	@NotNull(message = "")
	private Long id;
	@NotEmpty(message = "")
	private String title;
	private String description;
	private String[] prerequisities;
	private String[] outcomes;
	@NotNull(message = "")
	private TDState state;
	@NotNull(message = "")
	private Long startingLevel;
	private Set<AuthorRefDTO> authorRef = new HashSet<>();
	private SandboxDefinitionRefDTO sandBoxDefinitionRef;
	private boolean showStepperBar;

	public TrainingDefinitionUpdateDTO() {}

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

	public Long getStartingLevel() {
		return startingLevel;
	}

	public void setStartingLevel(Long startingLevel) {
		this.startingLevel = startingLevel;
	}

	public Set<AuthorRefDTO> getAuthorRef() {
		return authorRef;
	}

	public void setAuthorRef(Set<AuthorRefDTO> authorRef) {
		this.authorRef = authorRef;
	}

	public SandboxDefinitionRefDTO getSandBoxDefinitionRef() {
		return sandBoxDefinitionRef;
	}

	public void setSandBoxDefinitionRef(SandboxDefinitionRefDTO sandBoxDefinitionRef) {
		this.sandBoxDefinitionRef = sandBoxDefinitionRef;
	}

	public boolean isShowStepperBar() {
		return showStepperBar;
	}

	public void setShowStepperBar(boolean showStepperBar) {
		this.showStepperBar = showStepperBar;
	}

	@Override public String toString() {
		return "TrainingDefinitionUpdateDTO{" + "id=" + id + ", title='" + title + '\'' + ", description='" + description + '\''
				+ ", prerequisities=" + Arrays.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state
				+ ", startingLevel=" + startingLevel + ", authorRef=" + authorRef + ", sandBoxDefinitionRef=" + sandBoxDefinitionRef
				+ ", showStepperBar=" + showStepperBar + '}';
	}
}
