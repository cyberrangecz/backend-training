package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import java.util.Arrays;
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
@ApiModel(value = "TrainingDefinitionCreateDTO", description = "Training Definition to create.")
public class TrainingDefinitionCreateDTO {

	@NotEmpty(message = "")
	private String title;
	private String description;
	private String[] prerequisities;
	private String[] outcomes;
	@NotNull(message = "")
	private TDState state;
	@NotNull(message = "")
	Set<AuthorRefDTO> authorRef;
	@NotNull(message = "")
	boolean showStepperBar;
	@NotNull(message = "")
	SandboxDefinitionRefDTO sandboxDefinitionRefDTO;

	public TrainingDefinitionCreateDTO() {}

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

	public Set<AuthorRefDTO> getAuthorRef() {
		return authorRef;
	}

	public void setAuthorRef(Set<AuthorRefDTO> authorRef) {
		this.authorRef = authorRef;
	}

	public boolean isShowStepperBar() {
		return showStepperBar;
	}

	public void setShowStepperBar(boolean showStepperBar) {
		this.showStepperBar = showStepperBar;
	}

	public SandboxDefinitionRefDTO getSandboxDefinitionRefDTO() {
		return sandboxDefinitionRefDTO;
	}

	public void setSandboxDefinitionRefDTO(SandboxDefinitionRefDTO sandboxDefinitionRefDTO) {
		this.sandboxDefinitionRefDTO = sandboxDefinitionRefDTO;
	}

	@Override public String toString() {
		return "TrainingDefinitionCreateDTO{" + "title='" + title + '\'' + ", description='" + description + '\'' + ", prerequisities=" + Arrays
				.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state + ", authorRef=" + authorRef
				+ ", showStepperBar=" + showStepperBar + ", sandboxDefinitionRefDTO=" + sandboxDefinitionRefDTO + '}';
	}
}
